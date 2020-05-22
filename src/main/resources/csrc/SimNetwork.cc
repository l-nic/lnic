#include <vpi_user.h>
#include <svdpi.h>

#include <stdio.h>
#include <string>
#include <unordered_map>

#include "device.h"

std::unordered_map<std::string, NetworkDevice*> netdev_map;

extern "C" void network_init(const char *devname)
{
    netdev_map[std::string(devname)] = new NetworkDevice(devname);
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
        unsigned char *rx_last)
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

    netdev->tick_rx(rx_ready);

}
