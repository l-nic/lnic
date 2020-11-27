#ifndef __NET_PACKET_H__
#define __NET_PACKET_H__

#include <vpi_user.h>
#include <svdpi.h>

#include <stdlib.h>
#include <string.h>

#define ETH_MAX_WORDS 258
#define ETH_MAX_BYTES 2064

struct network_flit {
    uint64_t data;
    uint8_t keep;
    bool last;
};

struct network_packet {
  uint64_t data[ETH_MAX_WORDS];
  int len; // bytes
  int num_words;
};

static inline void init_network_packet(struct network_packet *packet)
{
    packet->len = 0;
    packet->num_words = 0;
    memset(packet->data, 0, ETH_MAX_WORDS * sizeof(uint64_t));
}

static inline void network_packet_add(network_packet *packet, uint64_t data, uint8_t keep)
{
    packet->data[packet->num_words] = data;
    packet->len += __builtin_popcount(keep);
    packet->num_words++;
}

#endif
