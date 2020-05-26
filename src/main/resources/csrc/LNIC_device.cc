#include <stdio.h>
#include <inttypes.h>

#include <arpa/inet.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/select.h>
#include <sys/time.h>
#include <fcntl.h>
#include <unistd.h>

#include <linux/if.h>
#include <linux/if_tun.h>

#include "LNIC_device.h"

static int tuntap_alloc(const char *dev, int flags)
{
  struct ifreq ifr;
  int fd, err;

  if ((fd = open("/dev/net/tun", O_RDWR)) < 0) {
    perror("open()");
    return fd;
  }

  memset(&ifr, 0, sizeof(ifr));

  ifr.ifr_flags = flags;
  strncpy(ifr.ifr_name, dev, IFNAMSIZ);

  if ((err = ioctl(fd, TUNSETIFF, &ifr)) < 0) {
    perror("ioctl()");
    close(fd);
    return err;
  }

  return fd;
}

NetworkDevice::NetworkDevice(const char *ifname)
{
    fd = tuntap_alloc(ifname, IFF_TAP | IFF_NO_PI);
    if (fd < 0) {
        fprintf(stderr, "Could not open tap interface\n");
        abort();
    }
}

NetworkDevice::~NetworkDevice()
{
    if (fd != -1)
        close(fd);
}

// TODO(sibanez): make sure that my memory management is legit
// TODO(sibanez): clean up headers
// TODO(sibanez): partition into functions

#define ceil_div(n, d) (((n) - 1) / (d) + 1)

void NetworkDevice::tick_tx(bool tx_valid,
                            uint64_t tx_data,
                            uint8_t tx_keep,
                            bool tx_last)
{

    /* Process TX flit.
     */ 
    if (tx_valid && tx_ready()) {
//        printf("Received flit from HW: %lx, %x, %d\n", tx_data, tx_keep, tx_last);
        struct network_flit flt;
        flt.data = tx_data;
        flt.keep = tx_keep;
        flt.last = tx_last;
        tx_flits.push(flt);
    }

    network_packet *send_packet;
    network_packet *recv_packet;

    /* If this is the last word then create a new packet and push to
     * the tx packet queue.
     */
    if (tx_valid && tx_ready() && tx_last) {
        send_packet = new network_packet;
        init_network_packet(send_packet);
        while (!tx_flits.empty()) {
            assert(send_packet->len < ETH_MAX_BYTES);
            network_packet_add(send_packet, tx_flits.front().data, tx_flits.front().keep);
            tx_flits.pop();
        }
//        printf("Adding new TX packet of length %d bytes ...\n", send_packet->len);
        tx_packets.push(send_packet);
    }

    /* Try to read / write tap interface. */

    fd_set rfds, wfds;
    struct timeval timeout;
    int retval;

    timeout.tv_sec = 1;
    timeout.tv_usec = 0;

    FD_ZERO(&rfds);
    FD_ZERO(&wfds);

    FD_SET(fd, &rfds);
    FD_SET(fd, &wfds);

    retval = select(fd + 1, &rfds, &wfds, NULL, &timeout);

    if (retval < 0) {
        perror("select()");
        abort();
    } else if (retval > 0) {

        /* If we are able to read from the tap iface then read the pkt and add to
         * incomming packet queue.
         */

        if (FD_ISSET(fd, &rfds)) {
//            printf("About to read tap iface ...\n");
            char *recv_buffer;

            recv_packet = new network_packet;
            init_network_packet(recv_packet);
            recv_buffer = ((char *) recv_packet->data);

            retval = read(fd, recv_buffer, ETH_MAX_BYTES);
            if (retval < 0) {
                perror("read()");
                abort();
            }
//            printf("Done reading %d bytes from tap iface ...\n", retval);

            recv_packet->len = retval;
            recv_packet->num_words = ceil_div(retval, sizeof(uint64_t));
            rx_packets.push(recv_packet);
        }

        /* If we have tx packets available and we are able to write to the
         * tap iface then write the first packet into the tap iface.
         */ 

        if (FD_ISSET(fd, &wfds) && !tx_packets.empty()) {
//            printf("About to write tap iface ...\n");
            char *send_buffer;
            size_t nbytes;

            send_packet = tx_packets.front();
            send_buffer = ((char *) send_packet->data);
            nbytes = send_packet->len;

            retval = write(fd, send_buffer, nbytes);
            if (retval < 0) {
                perror("write()");
                abort();
            }
            tx_packets.pop();
            delete send_packet;
//            printf("Done writing tap iface ...\n");
        }

    }

    /* Convert all rx packets to flits so that we can write to HW.
     */
    while (!rx_packets.empty()) {
//        printf("Converting pkt into flits ...\n");
        recv_packet = rx_packets.front();
        for (int i = 0; i < recv_packet->num_words; i++) {
            network_flit flt;
            flt.data = recv_packet->data[i];
            flt.last = (i + 1) == recv_packet->num_words;
            flt.keep = (flt.last && (recv_packet->len % sizeof(uint64_t) != 0))
                ? (1 << (recv_packet->len % sizeof(uint64_t))) - 1
                : 0xff;
            rx_flits.push(flt);
        }
        rx_packets.pop();
        delete recv_packet;
//        printf("Done converting pkt into flits ...\n");
    }
}


void NetworkDevice::tick_rx(bool rx_ready)
{
    /* Write flit to HW.
     */
    if (rx_valid() && rx_ready) {
//        printf("Writing flit to HW: %lx, %x, %d\n", rx_data(), rx_keep(), rx_last());
        rx_flits.pop();
    }
}
