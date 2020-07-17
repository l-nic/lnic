
import "DPI-C" function void network_tick (
    input string devname,

    input  bit      tx_valid,
    output bit      tx_ready,
    input  longint  tx_data,
    input  byte     tx_keep,
    input  bit      tx_last,

    output bit      rx_valid,
    input  bit      rx_ready,
    output longint  rx_data,
    output byte     rx_keep,
    output bit      rx_last,

    output longint  nic_mac_addr,
    output longint  switch_mac_addr,
    output int      nic_ip_addr,
    output longint  timeout_cycles,
    output shortint rtt_pkts
);

import "DPI-C" function void network_init (
    input string    devname,
    input longint   nic_mac_addr,
    input longint   switch_mac_addr,
    input int       nic_ip_addr,
    input longint   timeout_cycles,
    input shortint  rtt_pkts
);

module SimNetwork #(
  parameter DEVNAME = "tap0"
)
(
    input             clock,
    input             reset,

    // TX packets: HW --> tap iface
    input             net_out_valid,
    input  [63:0]     net_out_bits_data,
    input  [7:0]      net_out_bits_keep,
    input             net_out_bits_last,

    // RX packets: tap iface --> HW
    output reg        net_in_valid,
    output reg [63:0] net_in_bits_data,
    output reg [7:0]  net_in_bits_keep,
    output reg        net_in_bits_last,

    output [47:0] net_nic_mac_addr,
    output [47:0] net_switch_mac_addr,
    output [31:0] net_nic_ip_addr,
    output [63:0] net_timeout_cycles,
    output [15:0] net_rtt_pkts

);

    string devname = DEVNAME;
    longint nic_mac_addr = 0;
    longint switch_mac_addr = 0;
    int nic_ip_addr = 0;
    longint timeout_cycles = 0;
    shortint rtt_pkts = 0;

    // NOTE: currently unused
    reg net_out_ready;
    int dummy;

    longint _nic_mac_addr;
    reg [63:0] _nic_mac_addr_reg;
    longint _switch_mac_addr;
    reg [63:0] _switch_mac_addr_reg;
    int _nic_ip_addr;
    reg [31:0] _nic_ip_addr_reg;
    longint _timeout_cycles;
    reg [63:0] _timeout_cycles_reg;
    shortint _rtt_pkts;
    reg [15:0] _rtt_pkts_reg;

    initial begin
        dummy = $value$plusargs("nic_mac_addr=%h", nic_mac_addr);
        dummy = $value$plusargs("switch_mac_addr=%h", switch_mac_addr);
        dummy = $value$plusargs("nic_ip_addr=%h", nic_ip_addr);
        dummy = $value$plusargs("timeout_cycles=%d", timeout_cycles);
        dummy = $value$plusargs("rtt_pkts=%d", rtt_pkts);
        network_init(devname, nic_mac_addr, switch_mac_addr, nic_ip_addr, timeout_cycles, rtt_pkts);
    end

    always@(posedge clock) begin
        if (reset) begin
            net_out_ready = 0;
            net_in_valid = 0;
            net_in_bits_data = 0;
            net_in_bits_keep = 0;
            net_in_bits_last = 0;
        end
        else begin
            network_tick(
                devname,

                net_out_valid,
                net_out_ready,
                net_out_bits_data,
                net_out_bits_keep,
                net_out_bits_last,
    
                net_in_valid,
                1'b1,
                net_in_bits_data,
                net_in_bits_keep,
                net_in_bits_last,
                
                _nic_mac_addr,
                _switch_mac_addr,
                _nic_ip_addr,
                _timeout_cycles,
                _rtt_pkts);
            _nic_mac_addr_reg <= _nic_mac_addr;
            _switch_mac_addr_reg <= _switch_mac_addr;
            _nic_ip_addr_reg <= _nic_ip_addr;
            _timeout_cycles_reg <= _timeout_cycles;
            _rtt_pkts_reg <= _rtt_pkts;
        end
    end

    assign net_nic_mac_addr = _nic_mac_addr_reg[47:0];
    assign net_switch_mac_addr = _switch_mac_addr_reg[47:0];
    assign net_nic_ip_addr = _nic_ip_addr_reg;
    assign net_timeout_cycles = _timeout_cycles_reg;
    assign net_rtt_pkts = _rtt_pkts_reg;

endmodule
