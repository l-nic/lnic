
// *************************************************************************
// SDNetHomaIngress.sv
// *************************************************************************

module SDNetHomaIngress #(
  parameter TDATA_W = 512
) (
  // Packet In
  input                     net_net_in_valid,
  output                    net_net_in_ready,
  input       [TDATA_W-1:0] net_net_in_bits_data,
  input   [(TDATA_W/8)-1:0] net_net_in_bits_keep,
  input                     net_net_in_bits_last,

  // Packet Out
  output                    net_net_out_valid,
  input                     net_net_out_ready,
  output      [TDATA_W-1:0] net_net_out_bits_data,
  output  [(TDATA_W/8)-1:0] net_net_out_bits_keep,
  output                    net_net_out_bits_last,

  // Runtime Parameters
  input              [47:0] net_nic_mac_addr,
  input              [47:0] net_switch_mac_addr,
  input              [31:0] net_nic_ip_addr,
  input              [15:0] net_rtt_pkts,

  // Metadata Out
  output                    net_meta_out_valid,
  output             [31:0] net_meta_out_bits_src_ip,
  output             [15:0] net_meta_out_bits_src_context,
  output             [15:0] net_meta_out_bits_msg_len,
  output              [7:0] net_meta_out_bits_pkt_offset,
  output             [15:0] net_meta_out_bits_dst_context,
  output             [15:0] net_meta_out_bits_rx_msg_id,
  output             [15:0] net_meta_out_bits_tx_msg_id,
  output                    net_meta_out_bits_is_last_pkt,

  /* IO for get_rx_msg_info */
  output                    net_get_rx_msg_info_req_valid,
  output                    net_get_rx_msg_info_req_bits_mark_received,
  output             [31:0] net_get_rx_msg_info_req_bits_src_ip,
  output             [15:0] net_get_rx_msg_info_req_bits_src_context,
  output             [15:0] net_get_rx_msg_info_req_bits_tx_msg_id,
  output             [15:0] net_get_rx_msg_info_req_bits_msg_len,
  output              [7:0] net_get_rx_msg_info_req_bits_pkt_offset,
  input                     net_get_rx_msg_info_resp_valid,
  input                     net_get_rx_msg_info_resp_bits_fail,
  input              [15:0] net_get_rx_msg_info_resp_bits_rx_msg_id,
  input                     net_get_rx_msg_info_resp_bits_is_new_msg,
  input                     net_get_rx_msg_info_resp_bits_is_new_pkt,
  input                     net_get_rx_msg_info_resp_bits_is_last_pkt,
  input               [8:0] net_get_rx_msg_info_resp_bits_ackNo,

  /* IO for delivered event */
  output                    net_delivered_valid,
  output             [15:0] net_delivered_bits_tx_msg_id,
  output             [37:0] net_delivered_bits_delivered_pkts,
  output             [15:0] net_delivered_bits_msg_len,
  output             [15:0] net_delivered_bits_buf_ptr,
  output              [7:0] net_delivered_bits_buf_size_class,

  /* IO for creditToBtx event */
  output                    net_creditToBtx_valid,
  output             [15:0] net_creditToBtx_bits_tx_msg_id,
  output                    net_creditToBtx_bits_rtx,
  output              [7:0] net_creditToBtx_bits_rtx_pkt_offset,
  output                    net_creditToBtx_bits_update_credit,
  output             [15:0] net_creditToBtx_bits_new_credit,
  output             [15:0] net_creditToBtx_bits_buf_ptr,
  output              [7:0] net_creditToBtx_bits_buf_size_class,
  output             [31:0] net_creditToBtx_bits_dst_ip,
  output             [15:0] net_creditToBtx_bits_dst_context,
  output             [15:0] net_creditToBtx_bits_msg_len,
  output             [15:0] net_creditToBtx_bits_src_context,

  /* IO for ackPkt event */
  output                    net_ackPkt_valid,
  output             [31:0] net_ackPkt_bits_dst_ip,
  output             [15:0] net_ackPkt_bits_dst_context,
  output             [15:0] net_ackPkt_bits_msg_len,
  output              [7:0] net_ackPkt_bits_pkt_offset,
  output             [15:0] net_ackPkt_bits_src_context,
  output             [15:0] net_ackPkt_bits_tx_msg_id,
  output             [15:0] net_ackPkt_bits_buf_ptr,
  output              [7:0] net_ackPkt_bits_buf_size_class,
  output             [15:0] net_ackPkt_bits_credit,
  output              [7:0] net_ackPkt_bits_rank,
  output              [7:0] net_ackPkt_bits_flags,
  output                    net_ackPkt_bits_is_new_msg,
  output                    net_ackPkt_bits_is_rtx,

  /* IO for nackPkt event */
  output                    net_nackPkt_valid,
  output             [31:0] net_nackPkt_bits_dst_ip,
  output             [15:0] net_nackPkt_bits_dst_context,
  output             [15:0] net_nackPkt_bits_msg_len,
  output              [7:0] net_nackPkt_bits_pkt_offset,
  output             [15:0] net_nackPkt_bits_src_context,
  output             [15:0] net_nackPkt_bits_tx_msg_id,
  output             [15:0] net_nackPkt_bits_buf_ptr,
  output              [7:0] net_nackPkt_bits_buf_size_class,
  output             [15:0] net_nackPkt_bits_credit,
  output              [7:0] net_nackPkt_bits_rank,
  output              [7:0] net_nackPkt_bits_flags,
  output                    net_nackPkt_bits_is_new_msg,
  output                    net_nackPkt_bits_is_rtx,

  /* IO for grantPkt event */
  output                    net_grantPkt_valid,
  output             [31:0] net_grantPkt_bits_dst_ip,
  output             [15:0] net_grantPkt_bits_dst_context,
  output             [15:0] net_grantPkt_bits_msg_len,
  output              [7:0] net_grantPkt_bits_pkt_offset,
  output             [15:0] net_grantPkt_bits_src_context,
  output             [15:0] net_grantPkt_bits_tx_msg_id,
  output             [15:0] net_grantPkt_bits_buf_ptr,
  output              [7:0] net_grantPkt_bits_buf_size_class,
  output             [15:0] net_grantPkt_bits_credit,
  output              [7:0] net_grantPkt_bits_rank,
  output              [7:0] net_grantPkt_bits_flags,
  output                    net_grantPkt_bits_is_new_msg,
  output                    net_grantPkt_bits_is_rtx,

  /* IO for pendingMsgReg_cur_msg */
  output                      net_pendingMsgReg_cur_msg_req_valid,
  output               [15:0] net_pendingMsgReg_cur_msg_req_bits_index,
  output               [31:0] net_pendingMsgReg_cur_msg_req_bits_msg_info_src_ip,
  output               [15:0] net_pendingMsgReg_cur_msg_req_bits_msg_info_src_context,
  output               [15:0] net_pendingMsgReg_cur_msg_req_bits_msg_info_dst_context,
  output               [15:0] net_pendingMsgReg_cur_msg_req_bits_msg_info_tx_msg_id,
  output               [15:0] net_pendingMsgReg_cur_msg_req_bits_msg_info_msg_len,
  output               [15:0] net_pendingMsgReg_cur_msg_req_bits_msg_info_buf_ptr,
  output                [7:0] net_pendingMsgReg_cur_msg_req_bits_msg_info_buf_size_class,
  output                [8:0] net_pendingMsgReg_cur_msg_req_bits_msg_info_ackNo,
  output               [15:0] net_pendingMsgReg_cur_msg_req_bits_grant_info_grantedIdx,
  output               [15:0] net_pendingMsgReg_cur_msg_req_bits_grant_info_grantableIdx,
  output                [7:0] net_pendingMsgReg_cur_msg_req_bits_grant_info_remaining_size,
  output                      net_pendingMsgReg_cur_msg_req_bits_is_new_msg,

  input                       net_pendingMsgReg_cur_msg_resp_valid,
  input                [15:0] net_pendingMsgReg_cur_msg_resp_bits_grantedIdx,
  input                [15:0] net_pendingMsgReg_cur_msg_resp_bits_grantableIdx,
  input                 [7:0] net_pendingMsgReg_cur_msg_resp_bits_remaining_size,

  /* IO for pendingMsgReg_grant_msg */
  output                      net_pendingMsgReg_grant_msg_req_valid,
  output               [15:0] net_pendingMsgReg_grant_msg_req_bits_index,
  output               [15:0] net_pendingMsgReg_grant_msg_req_bits_grantedIdx,

  input                       net_pendingMsgReg_grant_msg_resp_valid,
  input                [31:0] net_pendingMsgReg_grant_msg_resp_bits_src_ip,
  input                [15:0] net_pendingMsgReg_grant_msg_resp_bits_src_context,
  input                [15:0] net_pendingMsgReg_grant_msg_resp_bits_dst_context,
  input                [15:0] net_pendingMsgReg_grant_msg_resp_bits_tx_msg_id,
  input                [15:0] net_pendingMsgReg_grant_msg_resp_bits_msg_len,
  input                [15:0] net_pendingMsgReg_grant_msg_resp_bits_buf_ptr,
  input                 [7:0] net_pendingMsgReg_grant_msg_resp_bits_buf_size_class,
  input                 [8:0] net_pendingMsgReg_grant_msg_resp_bits_ackNo,

  /* IO for grantScheduler */
  output                      net_grantScheduler_req_valid,
  output               [15:0] net_grantScheduler_req_bits_rx_msg_id,
  output                [7:0] net_grantScheduler_req_bits_rank,
  output                      net_grantScheduler_req_bits_removeObj,
  output               [15:0] net_grantScheduler_req_bits_grantableIdx,
  output               [15:0] net_grantScheduler_req_bits_grantedIdx,

  input                       net_grantScheduler_resp_valid,
  input                       net_grantScheduler_resp_bits_success,
  input                 [7:0] net_grantScheduler_resp_bits_prio_level,
  input                [15:0] net_grantScheduler_resp_bits_grant_offset,
  input                [15:0] net_grantScheduler_resp_bits_grant_msg_id,

  /* IO for txMsgPrioReg_req */
  output                      net_grantScheduler_req_valid,
  output               [15:0] net_grantScheduler_req_bits_index,
  output                [7:0] net_grantScheduler_req_bits_prio,

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

  reg                                      user_metadata_in_valid;
  sdnet_homa_ingress_pkg::USER_META_DATA_T user_metadata_in;
  wire                                     user_metadata_out_valid;
  sdnet_homa_ingress_pkg::USER_META_DATA_T user_metadata_out;

  sdnet_homa_ingress_pkg::USER_EXTERN_VALID_T user_extern_out_valid;
  sdnet_homa_ingress_pkg::USER_EXTERN_OUT_T   user_extern_out;
  sdnet_homa_ingress_pkg::USER_EXTERN_VALID_T user_extern_in_valid;
  sdnet_homa_ingress_pkg::USER_EXTERN_IN_T    user_extern_in;

  /* Output metadata */
  assign net_meta_out_valid = user_metadata_out_valid;
  assign {net_meta_out_bits_src_ip,
          net_meta_out_bits_src_context,
          net_meta_out_bits_msg_len,
          net_meta_out_bits_pkt_offset,
          net_meta_out_bits_dst_context,
          net_meta_out_bits_rx_msg_id,
          net_meta_out_bits_tx_msg_id,
          net_meta_out_bits_is_last_pkt} = user_metadata_out.meta;

  /* get_rx_msg_info extern */
  assign net_get_rx_msg_info_req_valid = user_extern_out_valid.get_rx_msg_info;
  assign {net_get_rx_msg_info_req_bits_mark_received,
          net_get_rx_msg_info_req_bits_src_ip,
          net_get_rx_msg_info_req_bits_src_context,
          net_get_rx_msg_info_req_bits_tx_msg_id,
          net_get_rx_msg_info_req_bits_msg_len,
          net_get_rx_msg_info_req_bits_pkt_offset} = user_extern_out.get_rx_msg_info;

  assign user_extern_in_valid.get_rx_msg_info = net_get_rx_msg_info_resp_valid;
  assign user_extern_in.get_rx_msg_info = {net_get_rx_msg_info_resp_bits_fail,
                                           net_get_rx_msg_info_resp_bits_rx_msg_id,
                                           net_get_rx_msg_info_resp_bits_is_new_msg,
                                           net_get_rx_msg_info_resp_bits_is_new_pkt,
                                           net_get_rx_msg_info_resp_bits_is_last_pkt,
                                           net_get_rx_msg_info_resp_bits_ackNo};

  /* delivered event */
  assign net_delivered_valid = user_extern_out_valid.delivered_event;
  assign {net_delivered_bits_tx_msg_id,
          net_delivered_bits_delivered_pkts,
          net_delivered_bits_msg_len,
          net_delivered_bits_buf_ptr,
          net_delivered_bits_buf_size_class} = user_extern_out.delivered_event;

  /* creditToBtx event */
  assign net_creditToBtx_valid = user_extern_out_valid.creditToBtx_event;
  assign {net_creditToBtx_bits_tx_msg_id,
          net_creditToBtx_bits_rtx,
          net_creditToBtx_bits_rtx_pkt_offset,
          net_creditToBtx_bits_update_credit,
          net_creditToBtx_bits_new_credit,
          net_creditToBtx_bits_buf_ptr,
          net_creditToBtx_bits_buf_size_class,
          net_creditToBtx_bits_dst_ip,
          net_creditToBtx_bits_dst_context,
          net_creditToBtx_bits_msg_len,
          net_creditToBtx_bits_src_context} = user_extern_out.creditToBtx_event;

  /* ackPkt event */
  assign net_ackPkt_valid = user_extern_out_valid.ackPkt_event;
  assign {net_ackPkt_bits_dst_ip,
          net_ackPkt_bits_dst_context,
          net_ackPkt_bits_msg_len,
          net_ackPkt_bits_pkt_offset,
          net_ackPkt_bits_src_context,
          net_ackPkt_bits_tx_msg_id,
          net_ackPkt_bits_buf_ptr,
          net_ackPkt_bits_buf_size_class,
          net_ackPkt_bits_credit,
          net_ackPkt_bits_rank,
          net_ackPkt_bits_flags,
          net_ackPkt_bits_is_new_msg,
          net_ackPkt_bits_is_rtx} = user_extern_out.ackPkt_event;

  /* nackPkt event */
  assign net_nackPkt_valid = user_extern_out_valid.nackPkt_event;
  assign {net_nackPkt_bits_dst_ip,
          net_nackPkt_bits_dst_context,
          net_nackPkt_bits_msg_len,
          net_nackPkt_bits_pkt_offset,
          net_nackPkt_bits_src_context,
          net_nackPkt_bits_tx_msg_id,
          net_nackPkt_bits_buf_ptr,
          net_nackPkt_bits_buf_size_class,
          net_nackPkt_bits_credit,
          net_nackPkt_bits_rank,
          net_nackPkt_bits_flags,
          net_nackPkt_bits_is_new_msg,
          net_nackPkt_bits_is_rtx} = user_extern_out.nackPkt_event;

  /* grantPkt event */
  assign net_grantPkt_valid = user_extern_out_valid.grantPkt_event;
  assign {net_grantPkt_bits_dst_ip,
          net_grantPkt_bits_dst_context,
          net_grantPkt_bits_msg_len,
          net_grantPkt_bits_pkt_offset,
          net_grantPkt_bits_src_context,
          net_grantPkt_bits_tx_msg_id,
          net_grantPkt_bits_buf_ptr,
          net_grantPkt_bits_buf_size_class,
          net_grantPkt_bits_credit,
          net_grantPkt_bits_rank,
          net_grantPkt_bits_flags,
          net_grantPkt_bits_is_new_msg,
          net_grantPkt_bits_is_rtx} = user_extern_out.grantPkt_event;

  /* pendingMsgReg_cur_msg extern */
  assign net_pendingMsgReg_cur_msg_req_valid = user_extern_out_valid.curMsgReg;
  assign {net_pendingMsgReg_cur_msg_req_bits_index,
          net_pendingMsgReg_cur_msg_req_bits_msg_info_src_ip,
          net_pendingMsgReg_cur_msg_req_bits_msg_info_src_context,
          net_pendingMsgReg_cur_msg_req_bits_msg_info_dst_context,
          net_pendingMsgReg_cur_msg_req_bits_msg_info_tx_msg_id,
          net_pendingMsgReg_cur_msg_req_bits_msg_info_msg_len,
          net_pendingMsgReg_cur_msg_req_bits_msg_info_buf_ptr,
          net_pendingMsgReg_cur_msg_req_bits_msg_info_buf_size_class,
          net_pendingMsgReg_cur_msg_req_bits_msg_info_ackNo,
          net_pendingMsgReg_cur_msg_req_bits_grant_info_grantedIdx,
          net_pendingMsgReg_cur_msg_req_bits_grant_info_grantableIdx,
          net_pendingMsgReg_cur_msg_req_bits_grant_info_remaining_size,
          net_pendingMsgReg_cur_msg_req_bits_is_new_msg} = user_extern_out.curMsgReg;

  assign user_extern_in_valid.curMsgReg = net_pendingMsgReg_cur_msg_resp_valid;
  assign user_extern_in.curMsgReg = {net_pendingMsgReg_cur_msg_resp_bits_grantedIdx,
                                     net_pendingMsgReg_cur_msg_resp_bits_grantableIdx,
                                     net_pendingMsgReg_cur_msg_resp_bits_remaining_size};

  /* pendingMsgReg_grant_msg extern */
  assign net_pendingMsgReg_grant_msg_req_valid = user_extern_out_valid.grantMsgReg;
  assign {net_pendingMsgReg_grant_msg_req_bits_index,
          net_pendingMsgReg_grant_msg_req_bits_grantedIdx} = user_extern_out.grantMsgReg;

  assign user_extern_in_valid.grantMsgReg = net_pendingMsgReg_grant_msg_resp_valid;
  assign user_extern_in.grantMsgReg = {net_pendingMsgReg_grant_msg_resp_bits_src_ip,
                                       net_pendingMsgReg_grant_msg_resp_bits_src_context,
                                       net_pendingMsgReg_grant_msg_resp_bits_dst_context,
                                       net_pendingMsgReg_grant_msg_resp_bits_tx_msg_id,
                                       net_pendingMsgReg_grant_msg_resp_bits_msg_len,
                                       net_pendingMsgReg_grant_msg_resp_bits_buf_ptr,
                                       net_pendingMsgReg_grant_msg_resp_bits_buf_size_class,
                                       net_pendingMsgReg_grant_msg_resp_bits_ackNo};

  /* grantScheduler extern */
  assign net_grantScheduler_req_valid = user_extern_out_valid.grantScheduler;
  assign {net_grantScheduler_req_bits_rx_msg_id,
          net_grantScheduler_req_bits_rank,
          net_grantScheduler_req_bits_removeObj,
          net_grantScheduler_req_bits_grantableIdx,
          net_grantScheduler_req_bits_grantedIdx} = user_extern_out.grantScheduler;

  assign user_extern_in_valid.grantScheduler = net_grantScheduler_resp_valid;
  assign user_extern_in.grantScheduler = {net_grantScheduler_resp_bits_success,
                                          net_grantScheduler_resp_bits_prio_level,
                                          net_grantScheduler_resp_bits_grant_offset,
                                          net_grantScheduler_resp_bits_grant_msg_id};

  /* txMsgPrioReg_req extern */
  assign net_grantScheduler_req_valid = user_extern_out_valid.txMsgPrioReg;
  assign {net_grantScheduler_req_bits_index,
          net_grantScheduler_req_bits_prio} = user_extern_out.txMsgPrioReg;


  // SDNet module
  sdnet_homa_ingress sdnet_homa_ingress_inst (
    // Clocks & Resets
    .s_axis_aclk             (clock),
    .s_axis_aresetn          (~reset),
    // NOTE: We will use the same clock for both AXI-Stream
    // and AXI-Lite. Usually, the AXI-Lite clock is much
    // slower, but since we'll run the clock @ ~100MHz, I
    // think this should be fine.
    // TODO(sibanez): Should check if Firesim is able to simulate designs
    // with multiple clock domains.
    .s_axi_aclk              (clock),
    .s_axi_aresetn           (~reset),
    // Metadata
    .user_metadata_in        (user_metadata_in),
    .user_metadata_in_valid  (user_metadata_in_valid),
    .user_metadata_out       (user_metadata_out),
    .user_metadata_out_valid (user_metadata_out_valid),
    // User Extern Data
    .user_extern_in          (user_extern_in),
    .user_extern_in_valid    (user_extern_in_valid),
    .user_extern_out         (user_extern_out),
    .user_extern_out_valid   (user_extern_out_valid),
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

  assign user_metadata_in.meta = 'b0;
  assign user_metadata_in.params = {net_nic_mac_addr,
                                    net_switch_mac_addr,
                                    net_nic_ip_addr,
                                    net_rtt_pkts};

  // State machine to drive user_metadata_in_valid
  reg state, next_state;
  localparam START = 0;
  localparam WAIT_EOP = 1;

  always @(*) begin
    // defaults
    next_state = state;
    user_metadata_in_valid = 0;

    case (state)
      START: begin
        if (net_net_in_valid && net_net_in_ready) begin
          user_metadata_in_valid = 1;
          if (!net_net_in_bits_last) begin
            next_state = WAIT_EOP;
          end
        end
      end
      WAIT_EOP: begin
        if (net_net_in_valid && net_net_in_ready && net_net_in_bits_last) begin
          next_state = START;
        end
      end
    endcase
  end

  always @(posedge clock) begin
    if (reset) begin
      state <= START;
    end
    else begin
      state <= next_state;
    end
  end

  /* Logic to drive dummy response logic for events */

  reg delivered_event_resp_valid;
  reg creditToBtx_event_resp_valid;
  reg ackPkt_event_resp_valid;
  reg nackPkt_event_resp_valid;
  reg grantPkt_event_resp_valid;
  reg txMsgPrioReg_resp_valid;
  reg dummy;

  always @(posedge clock) begin
    delivered_event_resp_valid   <= user_extern_out_valid.delivered_event;
    creditToBtx_event_resp_valid <= user_extern_out_valid.creditToBtx_event;
    ackPkt_event_resp_valid      <= user_extern_out_valid.ackPkt_event;
    nackPkt_event_resp_valid     <= user_extern_out_valid.nackPkt_event;
    grantPkt_event_resp_valid    <= user_extern_out_valid.grantPkt_event;
    txMsgPrioReg_resp_valid      <= user_extern_out_valid.txMsgPrioReg;
    dummy <= 0;
  end

  assign user_extern_in_valid.delivered_event   = delivered_event_resp_valid;
  assign user_extern_in_valid.creditToBtx_event = creditToBtx_event_resp_valid;
  assign user_extern_in_valid.ackPkt_event      = ackPkt_event_resp_valid;
  assign user_extern_in_valid.nackPkt_event     = nackPkt_event_resp_valid;
  assign user_extern_in_valid.grantPkt_event    = grantPkt_event_resp_valid;
  assign user_extern_in_valid.txMsgPrioReg      = txMsgPrioReg_resp_valid;

  assign user_extern_in.delivered_event   = dummy;
  assign user_extern_in.creditToBtx_event = dummy;
  assign user_extern_in.ackPkt_event      = dummy;
  assign user_extern_in.nackPkt_event     = dummy;
  assign user_extern_in.grantPkt_event    = dummy;
  assign user_extern_in.txMsgPrioReg      = dummy;

endmodule: SDNetHomaIngress
