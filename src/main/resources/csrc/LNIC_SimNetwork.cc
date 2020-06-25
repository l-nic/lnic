#include <vpi_user.h>
#include <svdpi.h>

#include <stdio.h>
#include <string>
#include <unordered_map>

#include "LNIC_device.h"

std::unordered_map<std::string, NetworkDevice*> netdev_map;

extern "C" void network_init(const char *devname, long long mac_addr)
{
    netdev_map[std::string(devname)] = new NetworkDevice(devname, mac_addr);
}

extern "C" void network_tick(
        const char *devname,

        unsigned char tx_valid,
        unsigned char *tx_ready,
        long long     tx_data,
        char          tx_keep,
        unsigned char tx_last,

        unsigned char *rx_valid,
        unsigned char rx_ready,
        long long     *rx_data,
        char          *rx_keep,
        unsigned char *rx_last,
        
        long long *mac_addr)
{
    NetworkDevice *netdev = netdev_map[std::string(devname)];

    if (!netdev) {
        *tx_ready = 0;
        *rx_valid = 0;
        *rx_data = 0;
        *rx_keep = 0;
        *rx_last = 0;
        return;
    }

    netdev->tick_tx(tx_valid, tx_data, tx_keep, tx_last);

    *tx_ready = netdev->tx_ready();
    *rx_valid = netdev->rx_valid();
    *rx_data = netdev->rx_data();
    *rx_keep = netdev->rx_keep();
    *rx_last = netdev->rx_last();

    *mac_addr = netdev->mac_addr();

    netdev->tick_rx(rx_ready);

}
