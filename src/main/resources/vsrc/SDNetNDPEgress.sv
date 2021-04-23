
// *************************************************************************
// SDNetNDPEgress.sv
// *************************************************************************

module SDNetNDPEgress #(
  parameter TDATA_W = 512
) (
  // Packet In
  input                     net_net_in_valid,
  output                    net_net_in_ready,
  input       [TDATA_W-1:0] net_net_in_bits_data,
  input   [(TDATA_W/8)-1:0] net_net_in_bits_keep,
  input                     net_net_in_bits_last,

  // Metadata In
  input                     net_meta_in_valid,
  input              [31:0] net_meta_in_bits_dst_ip,
  input              [15:0] net_meta_in_bits_dst_context,
  input              [15:0] net_meta_in_bits_msg_len,
  input               [7:0] net_meta_in_bits_pkt_offset,
  input              [15:0] net_meta_in_bits_src_context,
  input              [15:0] net_meta_in_bits_tx_msg_id,
  input              [15:0] net_meta_in_bits_buf_ptr,
  input               [7:0] net_meta_in_bits_buf_size_class,
  input              [15:0] net_meta_in_bits_credit,
  input               [7:0] net_meta_in_bits_rank,
  input               [7:0] net_meta_in_bits_flags,
  input                     net_meta_in_bits_is_new_msg,
  input                     net_meta_in_bits_is_rtx,

  // Runtime Parameters
  input              [47:0] net_nic_mac_addr,
  input              [47:0] net_switch_mac_addr,
  input              [31:0] net_nic_ip_addr,
  input              [15:0] net_rtt_pkts,

  // Packet Out
  output                    net_net_out_valid,
  input                     net_net_out_ready,
  output      [TDATA_W-1:0] net_net_out_bits_data,
  output  [(TDATA_W/8)-1:0] net_net_out_bits_keep,
  output                    net_net_out_bits_last,

  input                     reset,
  input                     clock
);

  // AXI-Lite Control Signals
  // TODO(sibanez): connect to sim_control
  wire                    s_axil_awvalid;
  wire             [31:0] s_axil_awaddr;
  wire                    s_axil_awready;
  wire                    s_axil_wvalid;
  wire             [31:0] s_axil_wdata;
  wire                    s_axil_wready;
  wire                    s_axil_bvalid;
  wire              [1:0] s_axil_bresp;
  wire                    s_axil_bready;
  wire                    s_axil_arvalid;
  wire             [31:0] s_axil_araddr;
  wire                    s_axil_arready;
  wire                    s_axil_rvalid;
  wire             [31:0] s_axil_rdata;
  wire              [1:0] s_axil_rresp;
  wire                    s_axil_rready;

  wire                                   user_metadata_in_valid;
  sdnet_ndp_egress_pkg::USER_META_DATA_T user_metadata_in;
  wire                                   user_metadata_out_valid;
  sdnet_ndp_egress_pkg::USER_META_DATA_T user_metadata_out;

  assign user_metadata_in_valid = net_meta_in_valid;
  assign user_metadata_in.meta = {net_meta_in_bits_dst_ip,
                                  net_meta_in_bits_dst_context,
                                  net_meta_in_bits_msg_len,
                                  net_meta_in_bits_pkt_offset,
                                  net_meta_in_bits_src_context,
                                  net_meta_in_bits_tx_msg_id,
                                  net_meta_in_bits_buf_ptr,
                                  net_meta_in_bits_buf_size_class,
                                  net_meta_in_bits_credit,
                                  net_meta_in_bits_rank,
                                  net_meta_in_bits_flags,
                                  net_meta_in_bits_is_new_msg,
                                  net_meta_in_bits_is_rtx};
  assign user_metadata_in.params = {net_nic_mac_addr,
                                    net_switch_mac_addr,
                                    net_nic_ip_addr,
                                    net_rtt_pkts};

  // SDNet module
  sdnet_ndp_egress sdnet_ndp_egress_inst (
    // Clocks & Resets
    .s_axis_aclk             (clock),
    .s_axis_aresetn          (~reset),
    // NOTE: We will use the same clock for both AXI-Stream
    // and AXI-Lite. Usually, the AXI-Lite clock is much
    // slower, but since we'll run the clock @ ~100MHz, I
    // think this should be fine.
    // TODO(sibanez): Should check if Firesim is even able to simulate designs
    // with multiple clock domains.
    .s_axi_aclk              (clock),
    .s_axi_aresetn           (~reset),
    // Metadata
    .user_metadata_in        (user_metadata_in),
    .user_metadata_in_valid  (user_metadata_in_valid),
    .user_metadata_out       (user_metadata_out),
    .user_metadata_out_valid (user_metadata_out_valid),
    // AXI4 Stream Slave port
    .s_axis_tvalid           (net_net_in_valid),
    .s_axis_tready           (net_net_in_ready),
    .s_axis_tdata            (net_net_in_bits_data),
    .s_axis_tkeep            (net_net_in_bits_keep),
    .s_axis_tlast            (net_net_in_bits_last),
    // AXI4 Stream Master port
    .m_axis_tvalid           (net_net_out_valid),
    .m_axis_tready           (net_net_out_ready),
    .m_axis_tdata            (net_net_out_bits_data),
    .m_axis_tkeep            (net_net_out_bits_keep),
    .m_axis_tlast            (net_net_out_bits_last),
     // Slave AXI-lite interface
    .s_axi_awaddr            (s_axil_awaddr),
    .s_axi_awvalid           (s_axil_awvalid),
    .s_axi_awready           (s_axil_awready),
    .s_axi_wdata             (s_axil_wdata),
    .s_axi_wstrb             (4'hF),
    .s_axi_wvalid            (s_axil_wvalid),
    .s_axi_wready            (s_axil_wready),
    .s_axi_bresp             (s_axil_bresp),
    .s_axi_bvalid            (s_axil_bvalid),
    .s_axi_bready            (s_axil_bready),
    .s_axi_araddr            (s_axil_araddr),
    .s_axi_arvalid           (s_axil_arvalid),
    .s_axi_arready           (s_axil_arready),
    .s_axi_rdata             (s_axil_rdata),
    .s_axi_rvalid            (s_axil_rvalid),
    .s_axi_rready            (s_axil_rready),
    .s_axi_rresp             (s_axil_rresp)
  );

endmodule: SDNetNDPEgress
