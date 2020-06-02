// Copyright 1986-2019 Xilinx, Inc. All Rights Reserved.
// --------------------------------------------------------------------------------
// Tool Version: Vivado v.2019.2 (lin64) Build 2708876 Wed Nov  6 21:39:14 MST 2019
// Date        : Thu May 28 19:38:05 2020
// Host        : localhost.localdomain running 64-bit unknown
// Command     : write_verilog -force -mode synth_stub /home/vagrant/chipyard/vivado/ip/sdnet_ingress/sdnet_ingress_stub.v
// Design      : sdnet_ingress
// Purpose     : Stub declaration of top-level module interface
// Device      : xcu250-figd2104-2L-e
// --------------------------------------------------------------------------------

// This empty module with port declaration file causes synthesis tools to infer a black box for IP.
// The synthesis directives are for Synopsys Synplify support to prevent IO buffer insertion.
// Please paste the declaration into a Verilog source file or add the file as an additional source.
module sdnet_ingress(s_axis_aclk, s_axis_aresetn, m_axi_hbm_aclk, 
  m_axi_hbm_aresetn, s_axi_aclk, s_axi_aresetn, cam_mem_aclk, cam_mem_aresetn, 
  user_metadata_in, user_metadata_in_valid, user_metadata_out, user_metadata_out_valid, 
  user_extern_in, user_extern_in_valid, user_extern_out, user_extern_out_valid, 
  m_axis_tdata, m_axis_tkeep, m_axis_tvalid, m_axis_tready, m_axis_tuser, m_axis_tid, 
  m_axis_tdest, m_axis_tlast, s_axis_tdata, s_axis_tkeep, s_axis_tvalid, s_axis_tlast, 
  s_axis_tuser, s_axis_tid, s_axis_tdest, s_axis_tready, m_axi_hbm00_awid, 
  m_axi_hbm00_awaddr, m_axi_hbm00_awlen, m_axi_hbm00_awsize, m_axi_hbm00_awburst, 
  m_axi_hbm00_awlock, m_axi_hbm00_awcache, m_axi_hbm00_awprot, m_axi_hbm00_awqos, 
  m_axi_hbm00_awregion, m_axi_hbm00_awvalid, m_axi_hbm00_awready, m_axi_hbm00_wid, 
  m_axi_hbm00_wdata, m_axi_hbm00_wstrb, m_axi_hbm00_wlast, m_axi_hbm00_wvalid, 
  m_axi_hbm00_wready, m_axi_hbm00_bid, m_axi_hbm00_bresp, m_axi_hbm00_bvalid, 
  m_axi_hbm00_bready, m_axi_hbm00_arid, m_axi_hbm00_araddr, m_axi_hbm00_arlen, 
  m_axi_hbm00_arsize, m_axi_hbm00_arburst, m_axi_hbm00_arlock, m_axi_hbm00_arcache, 
  m_axi_hbm00_arprot, m_axi_hbm00_arqos, m_axi_hbm00_arregion, m_axi_hbm00_arvalid, 
  m_axi_hbm00_arready, m_axi_hbm00_rid, m_axi_hbm00_rdata, m_axi_hbm00_rresp, 
  m_axi_hbm00_rlast, m_axi_hbm00_rvalid, m_axi_hbm00_rready, m_axi_hbm01_awid, 
  m_axi_hbm01_awaddr, m_axi_hbm01_awlen, m_axi_hbm01_awsize, m_axi_hbm01_awburst, 
  m_axi_hbm01_awlock, m_axi_hbm01_awcache, m_axi_hbm01_awprot, m_axi_hbm01_awqos, 
  m_axi_hbm01_awregion, m_axi_hbm01_awvalid, m_axi_hbm01_awready, m_axi_hbm01_wid, 
  m_axi_hbm01_wdata, m_axi_hbm01_wstrb, m_axi_hbm01_wlast, m_axi_hbm01_wvalid, 
  m_axi_hbm01_wready, m_axi_hbm01_bid, m_axi_hbm01_bresp, m_axi_hbm01_bvalid, 
  m_axi_hbm01_bready, m_axi_hbm01_arid, m_axi_hbm01_araddr, m_axi_hbm01_arlen, 
  m_axi_hbm01_arsize, m_axi_hbm01_arburst, m_axi_hbm01_arlock, m_axi_hbm01_arcache, 
  m_axi_hbm01_arprot, m_axi_hbm01_arqos, m_axi_hbm01_arregion, m_axi_hbm01_arvalid, 
  m_axi_hbm01_arready, m_axi_hbm01_rid, m_axi_hbm01_rdata, m_axi_hbm01_rresp, 
  m_axi_hbm01_rlast, m_axi_hbm01_rvalid, m_axi_hbm01_rready, m_axi_hbm02_awid, 
  m_axi_hbm02_awaddr, m_axi_hbm02_awlen, m_axi_hbm02_awsize, m_axi_hbm02_awburst, 
  m_axi_hbm02_awlock, m_axi_hbm02_awcache, m_axi_hbm02_awprot, m_axi_hbm02_awqos, 
  m_axi_hbm02_awregion, m_axi_hbm02_awvalid, m_axi_hbm02_awready, m_axi_hbm02_wid, 
  m_axi_hbm02_wdata, m_axi_hbm02_wstrb, m_axi_hbm02_wlast, m_axi_hbm02_wvalid, 
  m_axi_hbm02_wready, m_axi_hbm02_bid, m_axi_hbm02_bresp, m_axi_hbm02_bvalid, 
  m_axi_hbm02_bready, m_axi_hbm02_arid, m_axi_hbm02_araddr, m_axi_hbm02_arlen, 
  m_axi_hbm02_arsize, m_axi_hbm02_arburst, m_axi_hbm02_arlock, m_axi_hbm02_arcache, 
  m_axi_hbm02_arprot, m_axi_hbm02_arqos, m_axi_hbm02_arregion, m_axi_hbm02_arvalid, 
  m_axi_hbm02_arready, m_axi_hbm02_rid, m_axi_hbm02_rdata, m_axi_hbm02_rresp, 
  m_axi_hbm02_rlast, m_axi_hbm02_rvalid, m_axi_hbm02_rready, m_axi_hbm03_awid, 
  m_axi_hbm03_awaddr, m_axi_hbm03_awlen, m_axi_hbm03_awsize, m_axi_hbm03_awburst, 
  m_axi_hbm03_awlock, m_axi_hbm03_awcache, m_axi_hbm03_awprot, m_axi_hbm03_awqos, 
  m_axi_hbm03_awregion, m_axi_hbm03_awvalid, m_axi_hbm03_awready, m_axi_hbm03_wid, 
  m_axi_hbm03_wdata, m_axi_hbm03_wstrb, m_axi_hbm03_wlast, m_axi_hbm03_wvalid, 
  m_axi_hbm03_wready, m_axi_hbm03_bid, m_axi_hbm03_bresp, m_axi_hbm03_bvalid, 
  m_axi_hbm03_bready, m_axi_hbm03_arid, m_axi_hbm03_araddr, m_axi_hbm03_arlen, 
  m_axi_hbm03_arsize, m_axi_hbm03_arburst, m_axi_hbm03_arlock, m_axi_hbm03_arcache, 
  m_axi_hbm03_arprot, m_axi_hbm03_arqos, m_axi_hbm03_arregion, m_axi_hbm03_arvalid, 
  m_axi_hbm03_arready, m_axi_hbm03_rid, m_axi_hbm03_rdata, m_axi_hbm03_rresp, 
  m_axi_hbm03_rlast, m_axi_hbm03_rvalid, m_axi_hbm03_rready, m_axi_hbm04_awid, 
  m_axi_hbm04_awaddr, m_axi_hbm04_awlen, m_axi_hbm04_awsize, m_axi_hbm04_awburst, 
  m_axi_hbm04_awlock, m_axi_hbm04_awcache, m_axi_hbm04_awprot, m_axi_hbm04_awqos, 
  m_axi_hbm04_awregion, m_axi_hbm04_awvalid, m_axi_hbm04_awready, m_axi_hbm04_wid, 
  m_axi_hbm04_wdata, m_axi_hbm04_wstrb, m_axi_hbm04_wlast, m_axi_hbm04_wvalid, 
  m_axi_hbm04_wready, m_axi_hbm04_bid, m_axi_hbm04_bresp, m_axi_hbm04_bvalid, 
  m_axi_hbm04_bready, m_axi_hbm04_arid, m_axi_hbm04_araddr, m_axi_hbm04_arlen, 
  m_axi_hbm04_arsize, m_axi_hbm04_arburst, m_axi_hbm04_arlock, m_axi_hbm04_arcache, 
  m_axi_hbm04_arprot, m_axi_hbm04_arqos, m_axi_hbm04_arregion, m_axi_hbm04_arvalid, 
  m_axi_hbm04_arready, m_axi_hbm04_rid, m_axi_hbm04_rdata, m_axi_hbm04_rresp, 
  m_axi_hbm04_rlast, m_axi_hbm04_rvalid, m_axi_hbm04_rready, m_axi_hbm05_awid, 
  m_axi_hbm05_awaddr, m_axi_hbm05_awlen, m_axi_hbm05_awsize, m_axi_hbm05_awburst, 
  m_axi_hbm05_awlock, m_axi_hbm05_awcache, m_axi_hbm05_awprot, m_axi_hbm05_awqos, 
  m_axi_hbm05_awregion, m_axi_hbm05_awvalid, m_axi_hbm05_awready, m_axi_hbm05_wid, 
  m_axi_hbm05_wdata, m_axi_hbm05_wstrb, m_axi_hbm05_wlast, m_axi_hbm05_wvalid, 
  m_axi_hbm05_wready, m_axi_hbm05_bid, m_axi_hbm05_bresp, m_axi_hbm05_bvalid, 
  m_axi_hbm05_bready, m_axi_hbm05_arid, m_axi_hbm05_araddr, m_axi_hbm05_arlen, 
  m_axi_hbm05_arsize, m_axi_hbm05_arburst, m_axi_hbm05_arlock, m_axi_hbm05_arcache, 
  m_axi_hbm05_arprot, m_axi_hbm05_arqos, m_axi_hbm05_arregion, m_axi_hbm05_arvalid, 
  m_axi_hbm05_arready, m_axi_hbm05_rid, m_axi_hbm05_rdata, m_axi_hbm05_rresp, 
  m_axi_hbm05_rlast, m_axi_hbm05_rvalid, m_axi_hbm05_rready, m_axi_hbm06_awid, 
  m_axi_hbm06_awaddr, m_axi_hbm06_awlen, m_axi_hbm06_awsize, m_axi_hbm06_awburst, 
  m_axi_hbm06_awlock, m_axi_hbm06_awcache, m_axi_hbm06_awprot, m_axi_hbm06_awqos, 
  m_axi_hbm06_awregion, m_axi_hbm06_awvalid, m_axi_hbm06_awready, m_axi_hbm06_wid, 
  m_axi_hbm06_wdata, m_axi_hbm06_wstrb, m_axi_hbm06_wlast, m_axi_hbm06_wvalid, 
  m_axi_hbm06_wready, m_axi_hbm06_bid, m_axi_hbm06_bresp, m_axi_hbm06_bvalid, 
  m_axi_hbm06_bready, m_axi_hbm06_arid, m_axi_hbm06_araddr, m_axi_hbm06_arlen, 
  m_axi_hbm06_arsize, m_axi_hbm06_arburst, m_axi_hbm06_arlock, m_axi_hbm06_arcache, 
  m_axi_hbm06_arprot, m_axi_hbm06_arqos, m_axi_hbm06_arregion, m_axi_hbm06_arvalid, 
  m_axi_hbm06_arready, m_axi_hbm06_rid, m_axi_hbm06_rdata, m_axi_hbm06_rresp, 
  m_axi_hbm06_rlast, m_axi_hbm06_rvalid, m_axi_hbm06_rready, m_axi_hbm07_awid, 
  m_axi_hbm07_awaddr, m_axi_hbm07_awlen, m_axi_hbm07_awsize, m_axi_hbm07_awburst, 
  m_axi_hbm07_awlock, m_axi_hbm07_awcache, m_axi_hbm07_awprot, m_axi_hbm07_awqos, 
  m_axi_hbm07_awregion, m_axi_hbm07_awvalid, m_axi_hbm07_awready, m_axi_hbm07_wid, 
  m_axi_hbm07_wdata, m_axi_hbm07_wstrb, m_axi_hbm07_wlast, m_axi_hbm07_wvalid, 
  m_axi_hbm07_wready, m_axi_hbm07_bid, m_axi_hbm07_bresp, m_axi_hbm07_bvalid, 
  m_axi_hbm07_bready, m_axi_hbm07_arid, m_axi_hbm07_araddr, m_axi_hbm07_arlen, 
  m_axi_hbm07_arsize, m_axi_hbm07_arburst, m_axi_hbm07_arlock, m_axi_hbm07_arcache, 
  m_axi_hbm07_arprot, m_axi_hbm07_arqos, m_axi_hbm07_arregion, m_axi_hbm07_arvalid, 
  m_axi_hbm07_arready, m_axi_hbm07_rid, m_axi_hbm07_rdata, m_axi_hbm07_rresp, 
  m_axi_hbm07_rlast, m_axi_hbm07_rvalid, m_axi_hbm07_rready, m_axi_hbm08_awid, 
  m_axi_hbm08_awaddr, m_axi_hbm08_awlen, m_axi_hbm08_awsize, m_axi_hbm08_awburst, 
  m_axi_hbm08_awlock, m_axi_hbm08_awcache, m_axi_hbm08_awprot, m_axi_hbm08_awqos, 
  m_axi_hbm08_awregion, m_axi_hbm08_awvalid, m_axi_hbm08_awready, m_axi_hbm08_wid, 
  m_axi_hbm08_wdata, m_axi_hbm08_wstrb, m_axi_hbm08_wlast, m_axi_hbm08_wvalid, 
  m_axi_hbm08_wready, m_axi_hbm08_bid, m_axi_hbm08_bresp, m_axi_hbm08_bvalid, 
  m_axi_hbm08_bready, m_axi_hbm08_arid, m_axi_hbm08_araddr, m_axi_hbm08_arlen, 
  m_axi_hbm08_arsize, m_axi_hbm08_arburst, m_axi_hbm08_arlock, m_axi_hbm08_arcache, 
  m_axi_hbm08_arprot, m_axi_hbm08_arqos, m_axi_hbm08_arregion, m_axi_hbm08_arvalid, 
  m_axi_hbm08_arready, m_axi_hbm08_rid, m_axi_hbm08_rdata, m_axi_hbm08_rresp, 
  m_axi_hbm08_rlast, m_axi_hbm08_rvalid, m_axi_hbm08_rready, m_axi_hbm09_awid, 
  m_axi_hbm09_awaddr, m_axi_hbm09_awlen, m_axi_hbm09_awsize, m_axi_hbm09_awburst, 
  m_axi_hbm09_awlock, m_axi_hbm09_awcache, m_axi_hbm09_awprot, m_axi_hbm09_awqos, 
  m_axi_hbm09_awregion, m_axi_hbm09_awvalid, m_axi_hbm09_awready, m_axi_hbm09_wid, 
  m_axi_hbm09_wdata, m_axi_hbm09_wstrb, m_axi_hbm09_wlast, m_axi_hbm09_wvalid, 
  m_axi_hbm09_wready, m_axi_hbm09_bid, m_axi_hbm09_bresp, m_axi_hbm09_bvalid, 
  m_axi_hbm09_bready, m_axi_hbm09_arid, m_axi_hbm09_araddr, m_axi_hbm09_arlen, 
  m_axi_hbm09_arsize, m_axi_hbm09_arburst, m_axi_hbm09_arlock, m_axi_hbm09_arcache, 
  m_axi_hbm09_arprot, m_axi_hbm09_arqos, m_axi_hbm09_arregion, m_axi_hbm09_arvalid, 
  m_axi_hbm09_arready, m_axi_hbm09_rid, m_axi_hbm09_rdata, m_axi_hbm09_rresp, 
  m_axi_hbm09_rlast, m_axi_hbm09_rvalid, m_axi_hbm09_rready, m_axi_hbm10_awid, 
  m_axi_hbm10_awaddr, m_axi_hbm10_awlen, m_axi_hbm10_awsize, m_axi_hbm10_awburst, 
  m_axi_hbm10_awlock, m_axi_hbm10_awcache, m_axi_hbm10_awprot, m_axi_hbm10_awqos, 
  m_axi_hbm10_awregion, m_axi_hbm10_awvalid, m_axi_hbm10_awready, m_axi_hbm10_wid, 
  m_axi_hbm10_wdata, m_axi_hbm10_wstrb, m_axi_hbm10_wlast, m_axi_hbm10_wvalid, 
  m_axi_hbm10_wready, m_axi_hbm10_bid, m_axi_hbm10_bresp, m_axi_hbm10_bvalid, 
  m_axi_hbm10_bready, m_axi_hbm10_arid, m_axi_hbm10_araddr, m_axi_hbm10_arlen, 
  m_axi_hbm10_arsize, m_axi_hbm10_arburst, m_axi_hbm10_arlock, m_axi_hbm10_arcache, 
  m_axi_hbm10_arprot, m_axi_hbm10_arqos, m_axi_hbm10_arregion, m_axi_hbm10_arvalid, 
  m_axi_hbm10_arready, m_axi_hbm10_rid, m_axi_hbm10_rdata, m_axi_hbm10_rresp, 
  m_axi_hbm10_rlast, m_axi_hbm10_rvalid, m_axi_hbm10_rready, m_axi_hbm11_awid, 
  m_axi_hbm11_awaddr, m_axi_hbm11_awlen, m_axi_hbm11_awsize, m_axi_hbm11_awburst, 
  m_axi_hbm11_awlock, m_axi_hbm11_awcache, m_axi_hbm11_awprot, m_axi_hbm11_awqos, 
  m_axi_hbm11_awregion, m_axi_hbm11_awvalid, m_axi_hbm11_awready, m_axi_hbm11_wid, 
  m_axi_hbm11_wdata, m_axi_hbm11_wstrb, m_axi_hbm11_wlast, m_axi_hbm11_wvalid, 
  m_axi_hbm11_wready, m_axi_hbm11_bid, m_axi_hbm11_bresp, m_axi_hbm11_bvalid, 
  m_axi_hbm11_bready, m_axi_hbm11_arid, m_axi_hbm11_araddr, m_axi_hbm11_arlen, 
  m_axi_hbm11_arsize, m_axi_hbm11_arburst, m_axi_hbm11_arlock, m_axi_hbm11_arcache, 
  m_axi_hbm11_arprot, m_axi_hbm11_arqos, m_axi_hbm11_arregion, m_axi_hbm11_arvalid, 
  m_axi_hbm11_arready, m_axi_hbm11_rid, m_axi_hbm11_rdata, m_axi_hbm11_rresp, 
  m_axi_hbm11_rlast, m_axi_hbm11_rvalid, m_axi_hbm11_rready, m_axi_hbm12_awid, 
  m_axi_hbm12_awaddr, m_axi_hbm12_awlen, m_axi_hbm12_awsize, m_axi_hbm12_awburst, 
  m_axi_hbm12_awlock, m_axi_hbm12_awcache, m_axi_hbm12_awprot, m_axi_hbm12_awqos, 
  m_axi_hbm12_awregion, m_axi_hbm12_awvalid, m_axi_hbm12_awready, m_axi_hbm12_wid, 
  m_axi_hbm12_wdata, m_axi_hbm12_wstrb, m_axi_hbm12_wlast, m_axi_hbm12_wvalid, 
  m_axi_hbm12_wready, m_axi_hbm12_bid, m_axi_hbm12_bresp, m_axi_hbm12_bvalid, 
  m_axi_hbm12_bready, m_axi_hbm12_arid, m_axi_hbm12_araddr, m_axi_hbm12_arlen, 
  m_axi_hbm12_arsize, m_axi_hbm12_arburst, m_axi_hbm12_arlock, m_axi_hbm12_arcache, 
  m_axi_hbm12_arprot, m_axi_hbm12_arqos, m_axi_hbm12_arregion, m_axi_hbm12_arvalid, 
  m_axi_hbm12_arready, m_axi_hbm12_rid, m_axi_hbm12_rdata, m_axi_hbm12_rresp, 
  m_axi_hbm12_rlast, m_axi_hbm12_rvalid, m_axi_hbm12_rready, m_axi_hbm13_awid, 
  m_axi_hbm13_awaddr, m_axi_hbm13_awlen, m_axi_hbm13_awsize, m_axi_hbm13_awburst, 
  m_axi_hbm13_awlock, m_axi_hbm13_awcache, m_axi_hbm13_awprot, m_axi_hbm13_awqos, 
  m_axi_hbm13_awregion, m_axi_hbm13_awvalid, m_axi_hbm13_awready, m_axi_hbm13_wid, 
  m_axi_hbm13_wdata, m_axi_hbm13_wstrb, m_axi_hbm13_wlast, m_axi_hbm13_wvalid, 
  m_axi_hbm13_wready, m_axi_hbm13_bid, m_axi_hbm13_bresp, m_axi_hbm13_bvalid, 
  m_axi_hbm13_bready, m_axi_hbm13_arid, m_axi_hbm13_araddr, m_axi_hbm13_arlen, 
  m_axi_hbm13_arsize, m_axi_hbm13_arburst, m_axi_hbm13_arlock, m_axi_hbm13_arcache, 
  m_axi_hbm13_arprot, m_axi_hbm13_arqos, m_axi_hbm13_arregion, m_axi_hbm13_arvalid, 
  m_axi_hbm13_arready, m_axi_hbm13_rid, m_axi_hbm13_rdata, m_axi_hbm13_rresp, 
  m_axi_hbm13_rlast, m_axi_hbm13_rvalid, m_axi_hbm13_rready, m_axi_hbm14_awid, 
  m_axi_hbm14_awaddr, m_axi_hbm14_awlen, m_axi_hbm14_awsize, m_axi_hbm14_awburst, 
  m_axi_hbm14_awlock, m_axi_hbm14_awcache, m_axi_hbm14_awprot, m_axi_hbm14_awqos, 
  m_axi_hbm14_awregion, m_axi_hbm14_awvalid, m_axi_hbm14_awready, m_axi_hbm14_wid, 
  m_axi_hbm14_wdata, m_axi_hbm14_wstrb, m_axi_hbm14_wlast, m_axi_hbm14_wvalid, 
  m_axi_hbm14_wready, m_axi_hbm14_bid, m_axi_hbm14_bresp, m_axi_hbm14_bvalid, 
  m_axi_hbm14_bready, m_axi_hbm14_arid, m_axi_hbm14_araddr, m_axi_hbm14_arlen, 
  m_axi_hbm14_arsize, m_axi_hbm14_arburst, m_axi_hbm14_arlock, m_axi_hbm14_arcache, 
  m_axi_hbm14_arprot, m_axi_hbm14_arqos, m_axi_hbm14_arregion, m_axi_hbm14_arvalid, 
  m_axi_hbm14_arready, m_axi_hbm14_rid, m_axi_hbm14_rdata, m_axi_hbm14_rresp, 
  m_axi_hbm14_rlast, m_axi_hbm14_rvalid, m_axi_hbm14_rready, m_axi_hbm15_awid, 
  m_axi_hbm15_awaddr, m_axi_hbm15_awlen, m_axi_hbm15_awsize, m_axi_hbm15_awburst, 
  m_axi_hbm15_awlock, m_axi_hbm15_awcache, m_axi_hbm15_awprot, m_axi_hbm15_awqos, 
  m_axi_hbm15_awregion, m_axi_hbm15_awvalid, m_axi_hbm15_awready, m_axi_hbm15_wid, 
  m_axi_hbm15_wdata, m_axi_hbm15_wstrb, m_axi_hbm15_wlast, m_axi_hbm15_wvalid, 
  m_axi_hbm15_wready, m_axi_hbm15_bid, m_axi_hbm15_bresp, m_axi_hbm15_bvalid, 
  m_axi_hbm15_bready, m_axi_hbm15_arid, m_axi_hbm15_araddr, m_axi_hbm15_arlen, 
  m_axi_hbm15_arsize, m_axi_hbm15_arburst, m_axi_hbm15_arlock, m_axi_hbm15_arcache, 
  m_axi_hbm15_arprot, m_axi_hbm15_arqos, m_axi_hbm15_arregion, m_axi_hbm15_arvalid, 
  m_axi_hbm15_arready, m_axi_hbm15_rid, m_axi_hbm15_rdata, m_axi_hbm15_rresp, 
  m_axi_hbm15_rlast, m_axi_hbm15_rvalid, m_axi_hbm15_rready, m_axi_hbm16_awid, 
  m_axi_hbm16_awaddr, m_axi_hbm16_awlen, m_axi_hbm16_awsize, m_axi_hbm16_awburst, 
  m_axi_hbm16_awlock, m_axi_hbm16_awcache, m_axi_hbm16_awprot, m_axi_hbm16_awqos, 
  m_axi_hbm16_awregion, m_axi_hbm16_awvalid, m_axi_hbm16_awready, m_axi_hbm16_wid, 
  m_axi_hbm16_wdata, m_axi_hbm16_wstrb, m_axi_hbm16_wlast, m_axi_hbm16_wvalid, 
  m_axi_hbm16_wready, m_axi_hbm16_bid, m_axi_hbm16_bresp, m_axi_hbm16_bvalid, 
  m_axi_hbm16_bready, m_axi_hbm16_arid, m_axi_hbm16_araddr, m_axi_hbm16_arlen, 
  m_axi_hbm16_arsize, m_axi_hbm16_arburst, m_axi_hbm16_arlock, m_axi_hbm16_arcache, 
  m_axi_hbm16_arprot, m_axi_hbm16_arqos, m_axi_hbm16_arregion, m_axi_hbm16_arvalid, 
  m_axi_hbm16_arready, m_axi_hbm16_rid, m_axi_hbm16_rdata, m_axi_hbm16_rresp, 
  m_axi_hbm16_rlast, m_axi_hbm16_rvalid, m_axi_hbm16_rready, m_axi_hbm17_awid, 
  m_axi_hbm17_awaddr, m_axi_hbm17_awlen, m_axi_hbm17_awsize, m_axi_hbm17_awburst, 
  m_axi_hbm17_awlock, m_axi_hbm17_awcache, m_axi_hbm17_awprot, m_axi_hbm17_awqos, 
  m_axi_hbm17_awregion, m_axi_hbm17_awvalid, m_axi_hbm17_awready, m_axi_hbm17_wid, 
  m_axi_hbm17_wdata, m_axi_hbm17_wstrb, m_axi_hbm17_wlast, m_axi_hbm17_wvalid, 
  m_axi_hbm17_wready, m_axi_hbm17_bid, m_axi_hbm17_bresp, m_axi_hbm17_bvalid, 
  m_axi_hbm17_bready, m_axi_hbm17_arid, m_axi_hbm17_araddr, m_axi_hbm17_arlen, 
  m_axi_hbm17_arsize, m_axi_hbm17_arburst, m_axi_hbm17_arlock, m_axi_hbm17_arcache, 
  m_axi_hbm17_arprot, m_axi_hbm17_arqos, m_axi_hbm17_arregion, m_axi_hbm17_arvalid, 
  m_axi_hbm17_arready, m_axi_hbm17_rid, m_axi_hbm17_rdata, m_axi_hbm17_rresp, 
  m_axi_hbm17_rlast, m_axi_hbm17_rvalid, m_axi_hbm17_rready, m_axi_hbm18_awid, 
  m_axi_hbm18_awaddr, m_axi_hbm18_awlen, m_axi_hbm18_awsize, m_axi_hbm18_awburst, 
  m_axi_hbm18_awlock, m_axi_hbm18_awcache, m_axi_hbm18_awprot, m_axi_hbm18_awqos, 
  m_axi_hbm18_awregion, m_axi_hbm18_awvalid, m_axi_hbm18_awready, m_axi_hbm18_wid, 
  m_axi_hbm18_wdata, m_axi_hbm18_wstrb, m_axi_hbm18_wlast, m_axi_hbm18_wvalid, 
  m_axi_hbm18_wready, m_axi_hbm18_bid, m_axi_hbm18_bresp, m_axi_hbm18_bvalid, 
  m_axi_hbm18_bready, m_axi_hbm18_arid, m_axi_hbm18_araddr, m_axi_hbm18_arlen, 
  m_axi_hbm18_arsize, m_axi_hbm18_arburst, m_axi_hbm18_arlock, m_axi_hbm18_arcache, 
  m_axi_hbm18_arprot, m_axi_hbm18_arqos, m_axi_hbm18_arregion, m_axi_hbm18_arvalid, 
  m_axi_hbm18_arready, m_axi_hbm18_rid, m_axi_hbm18_rdata, m_axi_hbm18_rresp, 
  m_axi_hbm18_rlast, m_axi_hbm18_rvalid, m_axi_hbm18_rready, m_axi_hbm19_awid, 
  m_axi_hbm19_awaddr, m_axi_hbm19_awlen, m_axi_hbm19_awsize, m_axi_hbm19_awburst, 
  m_axi_hbm19_awlock, m_axi_hbm19_awcache, m_axi_hbm19_awprot, m_axi_hbm19_awqos, 
  m_axi_hbm19_awregion, m_axi_hbm19_awvalid, m_axi_hbm19_awready, m_axi_hbm19_wid, 
  m_axi_hbm19_wdata, m_axi_hbm19_wstrb, m_axi_hbm19_wlast, m_axi_hbm19_wvalid, 
  m_axi_hbm19_wready, m_axi_hbm19_bid, m_axi_hbm19_bresp, m_axi_hbm19_bvalid, 
  m_axi_hbm19_bready, m_axi_hbm19_arid, m_axi_hbm19_araddr, m_axi_hbm19_arlen, 
  m_axi_hbm19_arsize, m_axi_hbm19_arburst, m_axi_hbm19_arlock, m_axi_hbm19_arcache, 
  m_axi_hbm19_arprot, m_axi_hbm19_arqos, m_axi_hbm19_arregion, m_axi_hbm19_arvalid, 
  m_axi_hbm19_arready, m_axi_hbm19_rid, m_axi_hbm19_rdata, m_axi_hbm19_rresp, 
  m_axi_hbm19_rlast, m_axi_hbm19_rvalid, m_axi_hbm19_rready, m_axi_hbm20_awid, 
  m_axi_hbm20_awaddr, m_axi_hbm20_awlen, m_axi_hbm20_awsize, m_axi_hbm20_awburst, 
  m_axi_hbm20_awlock, m_axi_hbm20_awcache, m_axi_hbm20_awprot, m_axi_hbm20_awqos, 
  m_axi_hbm20_awregion, m_axi_hbm20_awvalid, m_axi_hbm20_awready, m_axi_hbm20_wid, 
  m_axi_hbm20_wdata, m_axi_hbm20_wstrb, m_axi_hbm20_wlast, m_axi_hbm20_wvalid, 
  m_axi_hbm20_wready, m_axi_hbm20_bid, m_axi_hbm20_bresp, m_axi_hbm20_bvalid, 
  m_axi_hbm20_bready, m_axi_hbm20_arid, m_axi_hbm20_araddr, m_axi_hbm20_arlen, 
  m_axi_hbm20_arsize, m_axi_hbm20_arburst, m_axi_hbm20_arlock, m_axi_hbm20_arcache, 
  m_axi_hbm20_arprot, m_axi_hbm20_arqos, m_axi_hbm20_arregion, m_axi_hbm20_arvalid, 
  m_axi_hbm20_arready, m_axi_hbm20_rid, m_axi_hbm20_rdata, m_axi_hbm20_rresp, 
  m_axi_hbm20_rlast, m_axi_hbm20_rvalid, m_axi_hbm20_rready, m_axi_hbm21_awid, 
  m_axi_hbm21_awaddr, m_axi_hbm21_awlen, m_axi_hbm21_awsize, m_axi_hbm21_awburst, 
  m_axi_hbm21_awlock, m_axi_hbm21_awcache, m_axi_hbm21_awprot, m_axi_hbm21_awqos, 
  m_axi_hbm21_awregion, m_axi_hbm21_awvalid, m_axi_hbm21_awready, m_axi_hbm21_wid, 
  m_axi_hbm21_wdata, m_axi_hbm21_wstrb, m_axi_hbm21_wlast, m_axi_hbm21_wvalid, 
  m_axi_hbm21_wready, m_axi_hbm21_bid, m_axi_hbm21_bresp, m_axi_hbm21_bvalid, 
  m_axi_hbm21_bready, m_axi_hbm21_arid, m_axi_hbm21_araddr, m_axi_hbm21_arlen, 
  m_axi_hbm21_arsize, m_axi_hbm21_arburst, m_axi_hbm21_arlock, m_axi_hbm21_arcache, 
  m_axi_hbm21_arprot, m_axi_hbm21_arqos, m_axi_hbm21_arregion, m_axi_hbm21_arvalid, 
  m_axi_hbm21_arready, m_axi_hbm21_rid, m_axi_hbm21_rdata, m_axi_hbm21_rresp, 
  m_axi_hbm21_rlast, m_axi_hbm21_rvalid, m_axi_hbm21_rready, m_axi_hbm22_awid, 
  m_axi_hbm22_awaddr, m_axi_hbm22_awlen, m_axi_hbm22_awsize, m_axi_hbm22_awburst, 
  m_axi_hbm22_awlock, m_axi_hbm22_awcache, m_axi_hbm22_awprot, m_axi_hbm22_awqos, 
  m_axi_hbm22_awregion, m_axi_hbm22_awvalid, m_axi_hbm22_awready, m_axi_hbm22_wid, 
  m_axi_hbm22_wdata, m_axi_hbm22_wstrb, m_axi_hbm22_wlast, m_axi_hbm22_wvalid, 
  m_axi_hbm22_wready, m_axi_hbm22_bid, m_axi_hbm22_bresp, m_axi_hbm22_bvalid, 
  m_axi_hbm22_bready, m_axi_hbm22_arid, m_axi_hbm22_araddr, m_axi_hbm22_arlen, 
  m_axi_hbm22_arsize, m_axi_hbm22_arburst, m_axi_hbm22_arlock, m_axi_hbm22_arcache, 
  m_axi_hbm22_arprot, m_axi_hbm22_arqos, m_axi_hbm22_arregion, m_axi_hbm22_arvalid, 
  m_axi_hbm22_arready, m_axi_hbm22_rid, m_axi_hbm22_rdata, m_axi_hbm22_rresp, 
  m_axi_hbm22_rlast, m_axi_hbm22_rvalid, m_axi_hbm22_rready, m_axi_hbm23_awid, 
  m_axi_hbm23_awaddr, m_axi_hbm23_awlen, m_axi_hbm23_awsize, m_axi_hbm23_awburst, 
  m_axi_hbm23_awlock, m_axi_hbm23_awcache, m_axi_hbm23_awprot, m_axi_hbm23_awqos, 
  m_axi_hbm23_awregion, m_axi_hbm23_awvalid, m_axi_hbm23_awready, m_axi_hbm23_wid, 
  m_axi_hbm23_wdata, m_axi_hbm23_wstrb, m_axi_hbm23_wlast, m_axi_hbm23_wvalid, 
  m_axi_hbm23_wready, m_axi_hbm23_bid, m_axi_hbm23_bresp, m_axi_hbm23_bvalid, 
  m_axi_hbm23_bready, m_axi_hbm23_arid, m_axi_hbm23_araddr, m_axi_hbm23_arlen, 
  m_axi_hbm23_arsize, m_axi_hbm23_arburst, m_axi_hbm23_arlock, m_axi_hbm23_arcache, 
  m_axi_hbm23_arprot, m_axi_hbm23_arqos, m_axi_hbm23_arregion, m_axi_hbm23_arvalid, 
  m_axi_hbm23_arready, m_axi_hbm23_rid, m_axi_hbm23_rdata, m_axi_hbm23_rresp, 
  m_axi_hbm23_rlast, m_axi_hbm23_rvalid, m_axi_hbm23_rready, m_axi_hbm24_awid, 
  m_axi_hbm24_awaddr, m_axi_hbm24_awlen, m_axi_hbm24_awsize, m_axi_hbm24_awburst, 
  m_axi_hbm24_awlock, m_axi_hbm24_awcache, m_axi_hbm24_awprot, m_axi_hbm24_awqos, 
  m_axi_hbm24_awregion, m_axi_hbm24_awvalid, m_axi_hbm24_awready, m_axi_hbm24_wid, 
  m_axi_hbm24_wdata, m_axi_hbm24_wstrb, m_axi_hbm24_wlast, m_axi_hbm24_wvalid, 
  m_axi_hbm24_wready, m_axi_hbm24_bid, m_axi_hbm24_bresp, m_axi_hbm24_bvalid, 
  m_axi_hbm24_bready, m_axi_hbm24_arid, m_axi_hbm24_araddr, m_axi_hbm24_arlen, 
  m_axi_hbm24_arsize, m_axi_hbm24_arburst, m_axi_hbm24_arlock, m_axi_hbm24_arcache, 
  m_axi_hbm24_arprot, m_axi_hbm24_arqos, m_axi_hbm24_arregion, m_axi_hbm24_arvalid, 
  m_axi_hbm24_arready, m_axi_hbm24_rid, m_axi_hbm24_rdata, m_axi_hbm24_rresp, 
  m_axi_hbm24_rlast, m_axi_hbm24_rvalid, m_axi_hbm24_rready, m_axi_hbm25_awid, 
  m_axi_hbm25_awaddr, m_axi_hbm25_awlen, m_axi_hbm25_awsize, m_axi_hbm25_awburst, 
  m_axi_hbm25_awlock, m_axi_hbm25_awcache, m_axi_hbm25_awprot, m_axi_hbm25_awqos, 
  m_axi_hbm25_awregion, m_axi_hbm25_awvalid, m_axi_hbm25_awready, m_axi_hbm25_wid, 
  m_axi_hbm25_wdata, m_axi_hbm25_wstrb, m_axi_hbm25_wlast, m_axi_hbm25_wvalid, 
  m_axi_hbm25_wready, m_axi_hbm25_bid, m_axi_hbm25_bresp, m_axi_hbm25_bvalid, 
  m_axi_hbm25_bready, m_axi_hbm25_arid, m_axi_hbm25_araddr, m_axi_hbm25_arlen, 
  m_axi_hbm25_arsize, m_axi_hbm25_arburst, m_axi_hbm25_arlock, m_axi_hbm25_arcache, 
  m_axi_hbm25_arprot, m_axi_hbm25_arqos, m_axi_hbm25_arregion, m_axi_hbm25_arvalid, 
  m_axi_hbm25_arready, m_axi_hbm25_rid, m_axi_hbm25_rdata, m_axi_hbm25_rresp, 
  m_axi_hbm25_rlast, m_axi_hbm25_rvalid, m_axi_hbm25_rready, m_axi_hbm26_awid, 
  m_axi_hbm26_awaddr, m_axi_hbm26_awlen, m_axi_hbm26_awsize, m_axi_hbm26_awburst, 
  m_axi_hbm26_awlock, m_axi_hbm26_awcache, m_axi_hbm26_awprot, m_axi_hbm26_awqos, 
  m_axi_hbm26_awregion, m_axi_hbm26_awvalid, m_axi_hbm26_awready, m_axi_hbm26_wid, 
  m_axi_hbm26_wdata, m_axi_hbm26_wstrb, m_axi_hbm26_wlast, m_axi_hbm26_wvalid, 
  m_axi_hbm26_wready, m_axi_hbm26_bid, m_axi_hbm26_bresp, m_axi_hbm26_bvalid, 
  m_axi_hbm26_bready, m_axi_hbm26_arid, m_axi_hbm26_araddr, m_axi_hbm26_arlen, 
  m_axi_hbm26_arsize, m_axi_hbm26_arburst, m_axi_hbm26_arlock, m_axi_hbm26_arcache, 
  m_axi_hbm26_arprot, m_axi_hbm26_arqos, m_axi_hbm26_arregion, m_axi_hbm26_arvalid, 
  m_axi_hbm26_arready, m_axi_hbm26_rid, m_axi_hbm26_rdata, m_axi_hbm26_rresp, 
  m_axi_hbm26_rlast, m_axi_hbm26_rvalid, m_axi_hbm26_rready, m_axi_hbm27_awid, 
  m_axi_hbm27_awaddr, m_axi_hbm27_awlen, m_axi_hbm27_awsize, m_axi_hbm27_awburst, 
  m_axi_hbm27_awlock, m_axi_hbm27_awcache, m_axi_hbm27_awprot, m_axi_hbm27_awqos, 
  m_axi_hbm27_awregion, m_axi_hbm27_awvalid, m_axi_hbm27_awready, m_axi_hbm27_wid, 
  m_axi_hbm27_wdata, m_axi_hbm27_wstrb, m_axi_hbm27_wlast, m_axi_hbm27_wvalid, 
  m_axi_hbm27_wready, m_axi_hbm27_bid, m_axi_hbm27_bresp, m_axi_hbm27_bvalid, 
  m_axi_hbm27_bready, m_axi_hbm27_arid, m_axi_hbm27_araddr, m_axi_hbm27_arlen, 
  m_axi_hbm27_arsize, m_axi_hbm27_arburst, m_axi_hbm27_arlock, m_axi_hbm27_arcache, 
  m_axi_hbm27_arprot, m_axi_hbm27_arqos, m_axi_hbm27_arregion, m_axi_hbm27_arvalid, 
  m_axi_hbm27_arready, m_axi_hbm27_rid, m_axi_hbm27_rdata, m_axi_hbm27_rresp, 
  m_axi_hbm27_rlast, m_axi_hbm27_rvalid, m_axi_hbm27_rready, m_axi_hbm28_awid, 
  m_axi_hbm28_awaddr, m_axi_hbm28_awlen, m_axi_hbm28_awsize, m_axi_hbm28_awburst, 
  m_axi_hbm28_awlock, m_axi_hbm28_awcache, m_axi_hbm28_awprot, m_axi_hbm28_awqos, 
  m_axi_hbm28_awregion, m_axi_hbm28_awvalid, m_axi_hbm28_awready, m_axi_hbm28_wid, 
  m_axi_hbm28_wdata, m_axi_hbm28_wstrb, m_axi_hbm28_wlast, m_axi_hbm28_wvalid, 
  m_axi_hbm28_wready, m_axi_hbm28_bid, m_axi_hbm28_bresp, m_axi_hbm28_bvalid, 
  m_axi_hbm28_bready, m_axi_hbm28_arid, m_axi_hbm28_araddr, m_axi_hbm28_arlen, 
  m_axi_hbm28_arsize, m_axi_hbm28_arburst, m_axi_hbm28_arlock, m_axi_hbm28_arcache, 
  m_axi_hbm28_arprot, m_axi_hbm28_arqos, m_axi_hbm28_arregion, m_axi_hbm28_arvalid, 
  m_axi_hbm28_arready, m_axi_hbm28_rid, m_axi_hbm28_rdata, m_axi_hbm28_rresp, 
  m_axi_hbm28_rlast, m_axi_hbm28_rvalid, m_axi_hbm28_rready, m_axi_hbm29_awid, 
  m_axi_hbm29_awaddr, m_axi_hbm29_awlen, m_axi_hbm29_awsize, m_axi_hbm29_awburst, 
  m_axi_hbm29_awlock, m_axi_hbm29_awcache, m_axi_hbm29_awprot, m_axi_hbm29_awqos, 
  m_axi_hbm29_awregion, m_axi_hbm29_awvalid, m_axi_hbm29_awready, m_axi_hbm29_wid, 
  m_axi_hbm29_wdata, m_axi_hbm29_wstrb, m_axi_hbm29_wlast, m_axi_hbm29_wvalid, 
  m_axi_hbm29_wready, m_axi_hbm29_bid, m_axi_hbm29_bresp, m_axi_hbm29_bvalid, 
  m_axi_hbm29_bready, m_axi_hbm29_arid, m_axi_hbm29_araddr, m_axi_hbm29_arlen, 
  m_axi_hbm29_arsize, m_axi_hbm29_arburst, m_axi_hbm29_arlock, m_axi_hbm29_arcache, 
  m_axi_hbm29_arprot, m_axi_hbm29_arqos, m_axi_hbm29_arregion, m_axi_hbm29_arvalid, 
  m_axi_hbm29_arready, m_axi_hbm29_rid, m_axi_hbm29_rdata, m_axi_hbm29_rresp, 
  m_axi_hbm29_rlast, m_axi_hbm29_rvalid, m_axi_hbm29_rready, m_axi_hbm30_awid, 
  m_axi_hbm30_awaddr, m_axi_hbm30_awlen, m_axi_hbm30_awsize, m_axi_hbm30_awburst, 
  m_axi_hbm30_awlock, m_axi_hbm30_awcache, m_axi_hbm30_awprot, m_axi_hbm30_awqos, 
  m_axi_hbm30_awregion, m_axi_hbm30_awvalid, m_axi_hbm30_awready, m_axi_hbm30_wid, 
  m_axi_hbm30_wdata, m_axi_hbm30_wstrb, m_axi_hbm30_wlast, m_axi_hbm30_wvalid, 
  m_axi_hbm30_wready, m_axi_hbm30_bid, m_axi_hbm30_bresp, m_axi_hbm30_bvalid, 
  m_axi_hbm30_bready, m_axi_hbm30_arid, m_axi_hbm30_araddr, m_axi_hbm30_arlen, 
  m_axi_hbm30_arsize, m_axi_hbm30_arburst, m_axi_hbm30_arlock, m_axi_hbm30_arcache, 
  m_axi_hbm30_arprot, m_axi_hbm30_arqos, m_axi_hbm30_arregion, m_axi_hbm30_arvalid, 
  m_axi_hbm30_arready, m_axi_hbm30_rid, m_axi_hbm30_rdata, m_axi_hbm30_rresp, 
  m_axi_hbm30_rlast, m_axi_hbm30_rvalid, m_axi_hbm30_rready, m_axi_hbm31_awid, 
  m_axi_hbm31_awaddr, m_axi_hbm31_awlen, m_axi_hbm31_awsize, m_axi_hbm31_awburst, 
  m_axi_hbm31_awlock, m_axi_hbm31_awcache, m_axi_hbm31_awprot, m_axi_hbm31_awqos, 
  m_axi_hbm31_awregion, m_axi_hbm31_awvalid, m_axi_hbm31_awready, m_axi_hbm31_wid, 
  m_axi_hbm31_wdata, m_axi_hbm31_wstrb, m_axi_hbm31_wlast, m_axi_hbm31_wvalid, 
  m_axi_hbm31_wready, m_axi_hbm31_bid, m_axi_hbm31_bresp, m_axi_hbm31_bvalid, 
  m_axi_hbm31_bready, m_axi_hbm31_arid, m_axi_hbm31_araddr, m_axi_hbm31_arlen, 
  m_axi_hbm31_arsize, m_axi_hbm31_arburst, m_axi_hbm31_arlock, m_axi_hbm31_arcache, 
  m_axi_hbm31_arprot, m_axi_hbm31_arqos, m_axi_hbm31_arregion, m_axi_hbm31_arvalid, 
  m_axi_hbm31_arready, m_axi_hbm31_rid, m_axi_hbm31_rdata, m_axi_hbm31_rresp, 
  m_axi_hbm31_rlast, m_axi_hbm31_rvalid, m_axi_hbm31_rready, s_axi_awaddr, s_axi_awvalid, 
  s_axi_awready, s_axi_wdata, s_axi_wstrb, s_axi_wvalid, s_axi_wready, s_axi_bresp, 
  s_axi_bvalid, s_axi_bready, s_axi_araddr, s_axi_arvalid, s_axi_arready, s_axi_rdata, 
  s_axi_rvalid, s_axi_rready, s_axi_rresp, irq)
/* synthesis syn_black_box black_box_pad_pin="s_axis_aclk,s_axis_aresetn,m_axi_hbm_aclk,m_axi_hbm_aresetn,s_axi_aclk,s_axi_aresetn,cam_mem_aclk,cam_mem_aresetn,user_metadata_in[119:0],user_metadata_in_valid,user_metadata_out[119:0],user_metadata_out_valid,user_extern_in[36:0],user_extern_in_valid[4:0],user_extern_out[501:0],user_extern_out_valid[4:0],m_axis_tdata[511:0],m_axis_tkeep[63:0],m_axis_tvalid,m_axis_tready,m_axis_tuser[0:0],m_axis_tid[0:0],m_axis_tdest[0:0],m_axis_tlast,s_axis_tdata[511:0],s_axis_tkeep[63:0],s_axis_tvalid,s_axis_tlast,s_axis_tuser[0:0],s_axis_tid[0:0],s_axis_tdest[0:0],s_axis_tready,m_axi_hbm00_awid[5:0],m_axi_hbm00_awaddr[32:0],m_axi_hbm00_awlen[3:0],m_axi_hbm00_awsize[2:0],m_axi_hbm00_awburst[1:0],m_axi_hbm00_awlock[1:0],m_axi_hbm00_awcache[3:0],m_axi_hbm00_awprot[2:0],m_axi_hbm00_awqos[3:0],m_axi_hbm00_awregion[3:0],m_axi_hbm00_awvalid,m_axi_hbm00_awready,m_axi_hbm00_wid[5:0],m_axi_hbm00_wdata[255:0],m_axi_hbm00_wstrb[31:0],m_axi_hbm00_wlast,m_axi_hbm00_wvalid,m_axi_hbm00_wready,m_axi_hbm00_bid[5:0],m_axi_hbm00_bresp[1:0],m_axi_hbm00_bvalid,m_axi_hbm00_bready,m_axi_hbm00_arid[5:0],m_axi_hbm00_araddr[32:0],m_axi_hbm00_arlen[3:0],m_axi_hbm00_arsize[2:0],m_axi_hbm00_arburst[1:0],m_axi_hbm00_arlock[1:0],m_axi_hbm00_arcache[3:0],m_axi_hbm00_arprot[2:0],m_axi_hbm00_arqos[3:0],m_axi_hbm00_arregion[3:0],m_axi_hbm00_arvalid,m_axi_hbm00_arready,m_axi_hbm00_rid[5:0],m_axi_hbm00_rdata[255:0],m_axi_hbm00_rresp[1:0],m_axi_hbm00_rlast,m_axi_hbm00_rvalid,m_axi_hbm00_rready,m_axi_hbm01_awid[5:0],m_axi_hbm01_awaddr[32:0],m_axi_hbm01_awlen[3:0],m_axi_hbm01_awsize[2:0],m_axi_hbm01_awburst[1:0],m_axi_hbm01_awlock[1:0],m_axi_hbm01_awcache[3:0],m_axi_hbm01_awprot[2:0],m_axi_hbm01_awqos[3:0],m_axi_hbm01_awregion[3:0],m_axi_hbm01_awvalid,m_axi_hbm01_awready,m_axi_hbm01_wid[5:0],m_axi_hbm01_wdata[255:0],m_axi_hbm01_wstrb[31:0],m_axi_hbm01_wlast,m_axi_hbm01_wvalid,m_axi_hbm01_wready,m_axi_hbm01_bid[5:0],m_axi_hbm01_bresp[1:0],m_axi_hbm01_bvalid,m_axi_hbm01_bready,m_axi_hbm01_arid[5:0],m_axi_hbm01_araddr[32:0],m_axi_hbm01_arlen[3:0],m_axi_hbm01_arsize[2:0],m_axi_hbm01_arburst[1:0],m_axi_hbm01_arlock[1:0],m_axi_hbm01_arcache[3:0],m_axi_hbm01_arprot[2:0],m_axi_hbm01_arqos[3:0],m_axi_hbm01_arregion[3:0],m_axi_hbm01_arvalid,m_axi_hbm01_arready,m_axi_hbm01_rid[5:0],m_axi_hbm01_rdata[255:0],m_axi_hbm01_rresp[1:0],m_axi_hbm01_rlast,m_axi_hbm01_rvalid,m_axi_hbm01_rready,m_axi_hbm02_awid[5:0],m_axi_hbm02_awaddr[32:0],m_axi_hbm02_awlen[3:0],m_axi_hbm02_awsize[2:0],m_axi_hbm02_awburst[1:0],m_axi_hbm02_awlock[1:0],m_axi_hbm02_awcache[3:0],m_axi_hbm02_awprot[2:0],m_axi_hbm02_awqos[3:0],m_axi_hbm02_awregion[3:0],m_axi_hbm02_awvalid,m_axi_hbm02_awready,m_axi_hbm02_wid[5:0],m_axi_hbm02_wdata[255:0],m_axi_hbm02_wstrb[31:0],m_axi_hbm02_wlast,m_axi_hbm02_wvalid,m_axi_hbm02_wready,m_axi_hbm02_bid[5:0],m_axi_hbm02_bresp[1:0],m_axi_hbm02_bvalid,m_axi_hbm02_bready,m_axi_hbm02_arid[5:0],m_axi_hbm02_araddr[32:0],m_axi_hbm02_arlen[3:0],m_axi_hbm02_arsize[2:0],m_axi_hbm02_arburst[1:0],m_axi_hbm02_arlock[1:0],m_axi_hbm02_arcache[3:0],m_axi_hbm02_arprot[2:0],m_axi_hbm02_arqos[3:0],m_axi_hbm02_arregion[3:0],m_axi_hbm02_arvalid,m_axi_hbm02_arready,m_axi_hbm02_rid[5:0],m_axi_hbm02_rdata[255:0],m_axi_hbm02_rresp[1:0],m_axi_hbm02_rlast,m_axi_hbm02_rvalid,m_axi_hbm02_rready,m_axi_hbm03_awid[5:0],m_axi_hbm03_awaddr[32:0],m_axi_hbm03_awlen[3:0],m_axi_hbm03_awsize[2:0],m_axi_hbm03_awburst[1:0],m_axi_hbm03_awlock[1:0],m_axi_hbm03_awcache[3:0],m_axi_hbm03_awprot[2:0],m_axi_hbm03_awqos[3:0],m_axi_hbm03_awregion[3:0],m_axi_hbm03_awvalid,m_axi_hbm03_awready,m_axi_hbm03_wid[5:0],m_axi_hbm03_wdata[255:0],m_axi_hbm03_wstrb[31:0],m_axi_hbm03_wlast,m_axi_hbm03_wvalid,m_axi_hbm03_wready,m_axi_hbm03_bid[5:0],m_axi_hbm03_bresp[1:0],m_axi_hbm03_bvalid,m_axi_hbm03_bready,m_axi_hbm03_arid[5:0],m_axi_hbm03_araddr[32:0],m_axi_hbm03_arlen[3:0],m_axi_hbm03_arsize[2:0],m_axi_hbm03_arburst[1:0],m_axi_hbm03_arlock[1:0],m_axi_hbm03_arcache[3:0],m_axi_hbm03_arprot[2:0],m_axi_hbm03_arqos[3:0],m_axi_hbm03_arregion[3:0],m_axi_hbm03_arvalid,m_axi_hbm03_arready,m_axi_hbm03_rid[5:0],m_axi_hbm03_rdata[255:0],m_axi_hbm03_rresp[1:0],m_axi_hbm03_rlast,m_axi_hbm03_rvalid,m_axi_hbm03_rready,m_axi_hbm04_awid[5:0],m_axi_hbm04_awaddr[32:0],m_axi_hbm04_awlen[3:0],m_axi_hbm04_awsize[2:0],m_axi_hbm04_awburst[1:0],m_axi_hbm04_awlock[1:0],m_axi_hbm04_awcache[3:0],m_axi_hbm04_awprot[2:0],m_axi_hbm04_awqos[3:0],m_axi_hbm04_awregion[3:0],m_axi_hbm04_awvalid,m_axi_hbm04_awready,m_axi_hbm04_wid[5:0],m_axi_hbm04_wdata[255:0],m_axi_hbm04_wstrb[31:0],m_axi_hbm04_wlast,m_axi_hbm04_wvalid,m_axi_hbm04_wready,m_axi_hbm04_bid[5:0],m_axi_hbm04_bresp[1:0],m_axi_hbm04_bvalid,m_axi_hbm04_bready,m_axi_hbm04_arid[5:0],m_axi_hbm04_araddr[32:0],m_axi_hbm04_arlen[3:0],m_axi_hbm04_arsize[2:0],m_axi_hbm04_arburst[1:0],m_axi_hbm04_arlock[1:0],m_axi_hbm04_arcache[3:0],m_axi_hbm04_arprot[2:0],m_axi_hbm04_arqos[3:0],m_axi_hbm04_arregion[3:0],m_axi_hbm04_arvalid,m_axi_hbm04_arready,m_axi_hbm04_rid[5:0],m_axi_hbm04_rdata[255:0],m_axi_hbm04_rresp[1:0],m_axi_hbm04_rlast,m_axi_hbm04_rvalid,m_axi_hbm04_rready,m_axi_hbm05_awid[5:0],m_axi_hbm05_awaddr[32:0],m_axi_hbm05_awlen[3:0],m_axi_hbm05_awsize[2:0],m_axi_hbm05_awburst[1:0],m_axi_hbm05_awlock[1:0],m_axi_hbm05_awcache[3:0],m_axi_hbm05_awprot[2:0],m_axi_hbm05_awqos[3:0],m_axi_hbm05_awregion[3:0],m_axi_hbm05_awvalid,m_axi_hbm05_awready,m_axi_hbm05_wid[5:0],m_axi_hbm05_wdata[255:0],m_axi_hbm05_wstrb[31:0],m_axi_hbm05_wlast,m_axi_hbm05_wvalid,m_axi_hbm05_wready,m_axi_hbm05_bid[5:0],m_axi_hbm05_bresp[1:0],m_axi_hbm05_bvalid,m_axi_hbm05_bready,m_axi_hbm05_arid[5:0],m_axi_hbm05_araddr[32:0],m_axi_hbm05_arlen[3:0],m_axi_hbm05_arsize[2:0],m_axi_hbm05_arburst[1:0],m_axi_hbm05_arlock[1:0],m_axi_hbm05_arcache[3:0],m_axi_hbm05_arprot[2:0],m_axi_hbm05_arqos[3:0],m_axi_hbm05_arregion[3:0],m_axi_hbm05_arvalid,m_axi_hbm05_arready,m_axi_hbm05_rid[5:0],m_axi_hbm05_rdata[255:0],m_axi_hbm05_rresp[1:0],m_axi_hbm05_rlast,m_axi_hbm05_rvalid,m_axi_hbm05_rready,m_axi_hbm06_awid[5:0],m_axi_hbm06_awaddr[32:0],m_axi_hbm06_awlen[3:0],m_axi_hbm06_awsize[2:0],m_axi_hbm06_awburst[1:0],m_axi_hbm06_awlock[1:0],m_axi_hbm06_awcache[3:0],m_axi_hbm06_awprot[2:0],m_axi_hbm06_awqos[3:0],m_axi_hbm06_awregion[3:0],m_axi_hbm06_awvalid,m_axi_hbm06_awready,m_axi_hbm06_wid[5:0],m_axi_hbm06_wdata[255:0],m_axi_hbm06_wstrb[31:0],m_axi_hbm06_wlast,m_axi_hbm06_wvalid,m_axi_hbm06_wready,m_axi_hbm06_bid[5:0],m_axi_hbm06_bresp[1:0],m_axi_hbm06_bvalid,m_axi_hbm06_bready,m_axi_hbm06_arid[5:0],m_axi_hbm06_araddr[32:0],m_axi_hbm06_arlen[3:0],m_axi_hbm06_arsize[2:0],m_axi_hbm06_arburst[1:0],m_axi_hbm06_arlock[1:0],m_axi_hbm06_arcache[3:0],m_axi_hbm06_arprot[2:0],m_axi_hbm06_arqos[3:0],m_axi_hbm06_arregion[3:0],m_axi_hbm06_arvalid,m_axi_hbm06_arready,m_axi_hbm06_rid[5:0],m_axi_hbm06_rdata[255:0],m_axi_hbm06_rresp[1:0],m_axi_hbm06_rlast,m_axi_hbm06_rvalid,m_axi_hbm06_rready,m_axi_hbm07_awid[5:0],m_axi_hbm07_awaddr[32:0],m_axi_hbm07_awlen[3:0],m_axi_hbm07_awsize[2:0],m_axi_hbm07_awburst[1:0],m_axi_hbm07_awlock[1:0],m_axi_hbm07_awcache[3:0],m_axi_hbm07_awprot[2:0],m_axi_hbm07_awqos[3:0],m_axi_hbm07_awregion[3:0],m_axi_hbm07_awvalid,m_axi_hbm07_awready,m_axi_hbm07_wid[5:0],m_axi_hbm07_wdata[255:0],m_axi_hbm07_wstrb[31:0],m_axi_hbm07_wlast,m_axi_hbm07_wvalid,m_axi_hbm07_wready,m_axi_hbm07_bid[5:0],m_axi_hbm07_bresp[1:0],m_axi_hbm07_bvalid,m_axi_hbm07_bready,m_axi_hbm07_arid[5:0],m_axi_hbm07_araddr[32:0],m_axi_hbm07_arlen[3:0],m_axi_hbm07_arsize[2:0],m_axi_hbm07_arburst[1:0],m_axi_hbm07_arlock[1:0],m_axi_hbm07_arcache[3:0],m_axi_hbm07_arprot[2:0],m_axi_hbm07_arqos[3:0],m_axi_hbm07_arregion[3:0],m_axi_hbm07_arvalid,m_axi_hbm07_arready,m_axi_hbm07_rid[5:0],m_axi_hbm07_rdata[255:0],m_axi_hbm07_rresp[1:0],m_axi_hbm07_rlast,m_axi_hbm07_rvalid,m_axi_hbm07_rready,m_axi_hbm08_awid[5:0],m_axi_hbm08_awaddr[32:0],m_axi_hbm08_awlen[3:0],m_axi_hbm08_awsize[2:0],m_axi_hbm08_awburst[1:0],m_axi_hbm08_awlock[1:0],m_axi_hbm08_awcache[3:0],m_axi_hbm08_awprot[2:0],m_axi_hbm08_awqos[3:0],m_axi_hbm08_awregion[3:0],m_axi_hbm08_awvalid,m_axi_hbm08_awready,m_axi_hbm08_wid[5:0],m_axi_hbm08_wdata[255:0],m_axi_hbm08_wstrb[31:0],m_axi_hbm08_wlast,m_axi_hbm08_wvalid,m_axi_hbm08_wready,m_axi_hbm08_bid[5:0],m_axi_hbm08_bresp[1:0],m_axi_hbm08_bvalid,m_axi_hbm08_bready,m_axi_hbm08_arid[5:0],m_axi_hbm08_araddr[32:0],m_axi_hbm08_arlen[3:0],m_axi_hbm08_arsize[2:0],m_axi_hbm08_arburst[1:0],m_axi_hbm08_arlock[1:0],m_axi_hbm08_arcache[3:0],m_axi_hbm08_arprot[2:0],m_axi_hbm08_arqos[3:0],m_axi_hbm08_arregion[3:0],m_axi_hbm08_arvalid,m_axi_hbm08_arready,m_axi_hbm08_rid[5:0],m_axi_hbm08_rdata[255:0],m_axi_hbm08_rresp[1:0],m_axi_hbm08_rlast,m_axi_hbm08_rvalid,m_axi_hbm08_rready,m_axi_hbm09_awid[5:0],m_axi_hbm09_awaddr[32:0],m_axi_hbm09_awlen[3:0],m_axi_hbm09_awsize[2:0],m_axi_hbm09_awburst[1:0],m_axi_hbm09_awlock[1:0],m_axi_hbm09_awcache[3:0],m_axi_hbm09_awprot[2:0],m_axi_hbm09_awqos[3:0],m_axi_hbm09_awregion[3:0],m_axi_hbm09_awvalid,m_axi_hbm09_awready,m_axi_hbm09_wid[5:0],m_axi_hbm09_wdata[255:0],m_axi_hbm09_wstrb[31:0],m_axi_hbm09_wlast,m_axi_hbm09_wvalid,m_axi_hbm09_wready,m_axi_hbm09_bid[5:0],m_axi_hbm09_bresp[1:0],m_axi_hbm09_bvalid,m_axi_hbm09_bready,m_axi_hbm09_arid[5:0],m_axi_hbm09_araddr[32:0],m_axi_hbm09_arlen[3:0],m_axi_hbm09_arsize[2:0],m_axi_hbm09_arburst[1:0],m_axi_hbm09_arlock[1:0],m_axi_hbm09_arcache[3:0],m_axi_hbm09_arprot[2:0],m_axi_hbm09_arqos[3:0],m_axi_hbm09_arregion[3:0],m_axi_hbm09_arvalid,m_axi_hbm09_arready,m_axi_hbm09_rid[5:0],m_axi_hbm09_rdata[255:0],m_axi_hbm09_rresp[1:0],m_axi_hbm09_rlast,m_axi_hbm09_rvalid,m_axi_hbm09_rready,m_axi_hbm10_awid[5:0],m_axi_hbm10_awaddr[32:0],m_axi_hbm10_awlen[3:0],m_axi_hbm10_awsize[2:0],m_axi_hbm10_awburst[1:0],m_axi_hbm10_awlock[1:0],m_axi_hbm10_awcache[3:0],m_axi_hbm10_awprot[2:0],m_axi_hbm10_awqos[3:0],m_axi_hbm10_awregion[3:0],m_axi_hbm10_awvalid,m_axi_hbm10_awready,m_axi_hbm10_wid[5:0],m_axi_hbm10_wdata[255:0],m_axi_hbm10_wstrb[31:0],m_axi_hbm10_wlast,m_axi_hbm10_wvalid,m_axi_hbm10_wready,m_axi_hbm10_bid[5:0],m_axi_hbm10_bresp[1:0],m_axi_hbm10_bvalid,m_axi_hbm10_bready,m_axi_hbm10_arid[5:0],m_axi_hbm10_araddr[32:0],m_axi_hbm10_arlen[3:0],m_axi_hbm10_arsize[2:0],m_axi_hbm10_arburst[1:0],m_axi_hbm10_arlock[1:0],m_axi_hbm10_arcache[3:0],m_axi_hbm10_arprot[2:0],m_axi_hbm10_arqos[3:0],m_axi_hbm10_arregion[3:0],m_axi_hbm10_arvalid,m_axi_hbm10_arready,m_axi_hbm10_rid[5:0],m_axi_hbm10_rdata[255:0],m_axi_hbm10_rresp[1:0],m_axi_hbm10_rlast,m_axi_hbm10_rvalid,m_axi_hbm10_rready,m_axi_hbm11_awid[5:0],m_axi_hbm11_awaddr[32:0],m_axi_hbm11_awlen[3:0],m_axi_hbm11_awsize[2:0],m_axi_hbm11_awburst[1:0],m_axi_hbm11_awlock[1:0],m_axi_hbm11_awcache[3:0],m_axi_hbm11_awprot[2:0],m_axi_hbm11_awqos[3:0],m_axi_hbm11_awregion[3:0],m_axi_hbm11_awvalid,m_axi_hbm11_awready,m_axi_hbm11_wid[5:0],m_axi_hbm11_wdata[255:0],m_axi_hbm11_wstrb[31:0],m_axi_hbm11_wlast,m_axi_hbm11_wvalid,m_axi_hbm11_wready,m_axi_hbm11_bid[5:0],m_axi_hbm11_bresp[1:0],m_axi_hbm11_bvalid,m_axi_hbm11_bready,m_axi_hbm11_arid[5:0],m_axi_hbm11_araddr[32:0],m_axi_hbm11_arlen[3:0],m_axi_hbm11_arsize[2:0],m_axi_hbm11_arburst[1:0],m_axi_hbm11_arlock[1:0],m_axi_hbm11_arcache[3:0],m_axi_hbm11_arprot[2:0],m_axi_hbm11_arqos[3:0],m_axi_hbm11_arregion[3:0],m_axi_hbm11_arvalid,m_axi_hbm11_arready,m_axi_hbm11_rid[5:0],m_axi_hbm11_rdata[255:0],m_axi_hbm11_rresp[1:0],m_axi_hbm11_rlast,m_axi_hbm11_rvalid,m_axi_hbm11_rready,m_axi_hbm12_awid[5:0],m_axi_hbm12_awaddr[32:0],m_axi_hbm12_awlen[3:0],m_axi_hbm12_awsize[2:0],m_axi_hbm12_awburst[1:0],m_axi_hbm12_awlock[1:0],m_axi_hbm12_awcache[3:0],m_axi_hbm12_awprot[2:0],m_axi_hbm12_awqos[3:0],m_axi_hbm12_awregion[3:0],m_axi_hbm12_awvalid,m_axi_hbm12_awready,m_axi_hbm12_wid[5:0],m_axi_hbm12_wdata[255:0],m_axi_hbm12_wstrb[31:0],m_axi_hbm12_wlast,m_axi_hbm12_wvalid,m_axi_hbm12_wready,m_axi_hbm12_bid[5:0],m_axi_hbm12_bresp[1:0],m_axi_hbm12_bvalid,m_axi_hbm12_bready,m_axi_hbm12_arid[5:0],m_axi_hbm12_araddr[32:0],m_axi_hbm12_arlen[3:0],m_axi_hbm12_arsize[2:0],m_axi_hbm12_arburst[1:0],m_axi_hbm12_arlock[1:0],m_axi_hbm12_arcache[3:0],m_axi_hbm12_arprot[2:0],m_axi_hbm12_arqos[3:0],m_axi_hbm12_arregion[3:0],m_axi_hbm12_arvalid,m_axi_hbm12_arready,m_axi_hbm12_rid[5:0],m_axi_hbm12_rdata[255:0],m_axi_hbm12_rresp[1:0],m_axi_hbm12_rlast,m_axi_hbm12_rvalid,m_axi_hbm12_rready,m_axi_hbm13_awid[5:0],m_axi_hbm13_awaddr[32:0],m_axi_hbm13_awlen[3:0],m_axi_hbm13_awsize[2:0],m_axi_hbm13_awburst[1:0],m_axi_hbm13_awlock[1:0],m_axi_hbm13_awcache[3:0],m_axi_hbm13_awprot[2:0],m_axi_hbm13_awqos[3:0],m_axi_hbm13_awregion[3:0],m_axi_hbm13_awvalid,m_axi_hbm13_awready,m_axi_hbm13_wid[5:0],m_axi_hbm13_wdata[255:0],m_axi_hbm13_wstrb[31:0],m_axi_hbm13_wlast,m_axi_hbm13_wvalid,m_axi_hbm13_wready,m_axi_hbm13_bid[5:0],m_axi_hbm13_bresp[1:0],m_axi_hbm13_bvalid,m_axi_hbm13_bready,m_axi_hbm13_arid[5:0],m_axi_hbm13_araddr[32:0],m_axi_hbm13_arlen[3:0],m_axi_hbm13_arsize[2:0],m_axi_hbm13_arburst[1:0],m_axi_hbm13_arlock[1:0],m_axi_hbm13_arcache[3:0],m_axi_hbm13_arprot[2:0],m_axi_hbm13_arqos[3:0],m_axi_hbm13_arregion[3:0],m_axi_hbm13_arvalid,m_axi_hbm13_arready,m_axi_hbm13_rid[5:0],m_axi_hbm13_rdata[255:0],m_axi_hbm13_rresp[1:0],m_axi_hbm13_rlast,m_axi_hbm13_rvalid,m_axi_hbm13_rready,m_axi_hbm14_awid[5:0],m_axi_hbm14_awaddr[32:0],m_axi_hbm14_awlen[3:0],m_axi_hbm14_awsize[2:0],m_axi_hbm14_awburst[1:0],m_axi_hbm14_awlock[1:0],m_axi_hbm14_awcache[3:0],m_axi_hbm14_awprot[2:0],m_axi_hbm14_awqos[3:0],m_axi_hbm14_awregion[3:0],m_axi_hbm14_awvalid,m_axi_hbm14_awready,m_axi_hbm14_wid[5:0],m_axi_hbm14_wdata[255:0],m_axi_hbm14_wstrb[31:0],m_axi_hbm14_wlast,m_axi_hbm14_wvalid,m_axi_hbm14_wready,m_axi_hbm14_bid[5:0],m_axi_hbm14_bresp[1:0],m_axi_hbm14_bvalid,m_axi_hbm14_bready,m_axi_hbm14_arid[5:0],m_axi_hbm14_araddr[32:0],m_axi_hbm14_arlen[3:0],m_axi_hbm14_arsize[2:0],m_axi_hbm14_arburst[1:0],m_axi_hbm14_arlock[1:0],m_axi_hbm14_arcache[3:0],m_axi_hbm14_arprot[2:0],m_axi_hbm14_arqos[3:0],m_axi_hbm14_arregion[3:0],m_axi_hbm14_arvalid,m_axi_hbm14_arready,m_axi_hbm14_rid[5:0],m_axi_hbm14_rdata[255:0],m_axi_hbm14_rresp[1:0],m_axi_hbm14_rlast,m_axi_hbm14_rvalid,m_axi_hbm14_rready,m_axi_hbm15_awid[5:0],m_axi_hbm15_awaddr[32:0],m_axi_hbm15_awlen[3:0],m_axi_hbm15_awsize[2:0],m_axi_hbm15_awburst[1:0],m_axi_hbm15_awlock[1:0],m_axi_hbm15_awcache[3:0],m_axi_hbm15_awprot[2:0],m_axi_hbm15_awqos[3:0],m_axi_hbm15_awregion[3:0],m_axi_hbm15_awvalid,m_axi_hbm15_awready,m_axi_hbm15_wid[5:0],m_axi_hbm15_wdata[255:0],m_axi_hbm15_wstrb[31:0],m_axi_hbm15_wlast,m_axi_hbm15_wvalid,m_axi_hbm15_wready,m_axi_hbm15_bid[5:0],m_axi_hbm15_bresp[1:0],m_axi_hbm15_bvalid,m_axi_hbm15_bready,m_axi_hbm15_arid[5:0],m_axi_hbm15_araddr[32:0],m_axi_hbm15_arlen[3:0],m_axi_hbm15_arsize[2:0],m_axi_hbm15_arburst[1:0],m_axi_hbm15_arlock[1:0],m_axi_hbm15_arcache[3:0],m_axi_hbm15_arprot[2:0],m_axi_hbm15_arqos[3:0],m_axi_hbm15_arregion[3:0],m_axi_hbm15_arvalid,m_axi_hbm15_arready,m_axi_hbm15_rid[5:0],m_axi_hbm15_rdata[255:0],m_axi_hbm15_rresp[1:0],m_axi_hbm15_rlast,m_axi_hbm15_rvalid,m_axi_hbm15_rready,m_axi_hbm16_awid[5:0],m_axi_hbm16_awaddr[32:0],m_axi_hbm16_awlen[3:0],m_axi_hbm16_awsize[2:0],m_axi_hbm16_awburst[1:0],m_axi_hbm16_awlock[1:0],m_axi_hbm16_awcache[3:0],m_axi_hbm16_awprot[2:0],m_axi_hbm16_awqos[3:0],m_axi_hbm16_awregion[3:0],m_axi_hbm16_awvalid,m_axi_hbm16_awready,m_axi_hbm16_wid[5:0],m_axi_hbm16_wdata[255:0],m_axi_hbm16_wstrb[31:0],m_axi_hbm16_wlast,m_axi_hbm16_wvalid,m_axi_hbm16_wready,m_axi_hbm16_bid[5:0],m_axi_hbm16_bresp[1:0],m_axi_hbm16_bvalid,m_axi_hbm16_bready,m_axi_hbm16_arid[5:0],m_axi_hbm16_araddr[32:0],m_axi_hbm16_arlen[3:0],m_axi_hbm16_arsize[2:0],m_axi_hbm16_arburst[1:0],m_axi_hbm16_arlock[1:0],m_axi_hbm16_arcache[3:0],m_axi_hbm16_arprot[2:0],m_axi_hbm16_arqos[3:0],m_axi_hbm16_arregion[3:0],m_axi_hbm16_arvalid,m_axi_hbm16_arready,m_axi_hbm16_rid[5:0],m_axi_hbm16_rdata[255:0],m_axi_hbm16_rresp[1:0],m_axi_hbm16_rlast,m_axi_hbm16_rvalid,m_axi_hbm16_rready,m_axi_hbm17_awid[5:0],m_axi_hbm17_awaddr[32:0],m_axi_hbm17_awlen[3:0],m_axi_hbm17_awsize[2:0],m_axi_hbm17_awburst[1:0],m_axi_hbm17_awlock[1:0],m_axi_hbm17_awcache[3:0],m_axi_hbm17_awprot[2:0],m_axi_hbm17_awqos[3:0],m_axi_hbm17_awregion[3:0],m_axi_hbm17_awvalid,m_axi_hbm17_awready,m_axi_hbm17_wid[5:0],m_axi_hbm17_wdata[255:0],m_axi_hbm17_wstrb[31:0],m_axi_hbm17_wlast,m_axi_hbm17_wvalid,m_axi_hbm17_wready,m_axi_hbm17_bid[5:0],m_axi_hbm17_bresp[1:0],m_axi_hbm17_bvalid,m_axi_hbm17_bready,m_axi_hbm17_arid[5:0],m_axi_hbm17_araddr[32:0],m_axi_hbm17_arlen[3:0],m_axi_hbm17_arsize[2:0],m_axi_hbm17_arburst[1:0],m_axi_hbm17_arlock[1:0],m_axi_hbm17_arcache[3:0],m_axi_hbm17_arprot[2:0],m_axi_hbm17_arqos[3:0],m_axi_hbm17_arregion[3:0],m_axi_hbm17_arvalid,m_axi_hbm17_arready,m_axi_hbm17_rid[5:0],m_axi_hbm17_rdata[255:0],m_axi_hbm17_rresp[1:0],m_axi_hbm17_rlast,m_axi_hbm17_rvalid,m_axi_hbm17_rready,m_axi_hbm18_awid[5:0],m_axi_hbm18_awaddr[32:0],m_axi_hbm18_awlen[3:0],m_axi_hbm18_awsize[2:0],m_axi_hbm18_awburst[1:0],m_axi_hbm18_awlock[1:0],m_axi_hbm18_awcache[3:0],m_axi_hbm18_awprot[2:0],m_axi_hbm18_awqos[3:0],m_axi_hbm18_awregion[3:0],m_axi_hbm18_awvalid,m_axi_hbm18_awready,m_axi_hbm18_wid[5:0],m_axi_hbm18_wdata[255:0],m_axi_hbm18_wstrb[31:0],m_axi_hbm18_wlast,m_axi_hbm18_wvalid,m_axi_hbm18_wready,m_axi_hbm18_bid[5:0],m_axi_hbm18_bresp[1:0],m_axi_hbm18_bvalid,m_axi_hbm18_bready,m_axi_hbm18_arid[5:0],m_axi_hbm18_araddr[32:0],m_axi_hbm18_arlen[3:0],m_axi_hbm18_arsize[2:0],m_axi_hbm18_arburst[1:0],m_axi_hbm18_arlock[1:0],m_axi_hbm18_arcache[3:0],m_axi_hbm18_arprot[2:0],m_axi_hbm18_arqos[3:0],m_axi_hbm18_arregion[3:0],m_axi_hbm18_arvalid,m_axi_hbm18_arready,m_axi_hbm18_rid[5:0],m_axi_hbm18_rdata[255:0],m_axi_hbm18_rresp[1:0],m_axi_hbm18_rlast,m_axi_hbm18_rvalid,m_axi_hbm18_rready,m_axi_hbm19_awid[5:0],m_axi_hbm19_awaddr[32:0],m_axi_hbm19_awlen[3:0],m_axi_hbm19_awsize[2:0],m_axi_hbm19_awburst[1:0],m_axi_hbm19_awlock[1:0],m_axi_hbm19_awcache[3:0],m_axi_hbm19_awprot[2:0],m_axi_hbm19_awqos[3:0],m_axi_hbm19_awregion[3:0],m_axi_hbm19_awvalid,m_axi_hbm19_awready,m_axi_hbm19_wid[5:0],m_axi_hbm19_wdata[255:0],m_axi_hbm19_wstrb[31:0],m_axi_hbm19_wlast,m_axi_hbm19_wvalid,m_axi_hbm19_wready,m_axi_hbm19_bid[5:0],m_axi_hbm19_bresp[1:0],m_axi_hbm19_bvalid,m_axi_hbm19_bready,m_axi_hbm19_arid[5:0],m_axi_hbm19_araddr[32:0],m_axi_hbm19_arlen[3:0],m_axi_hbm19_arsize[2:0],m_axi_hbm19_arburst[1:0],m_axi_hbm19_arlock[1:0],m_axi_hbm19_arcache[3:0],m_axi_hbm19_arprot[2:0],m_axi_hbm19_arqos[3:0],m_axi_hbm19_arregion[3:0],m_axi_hbm19_arvalid,m_axi_hbm19_arready,m_axi_hbm19_rid[5:0],m_axi_hbm19_rdata[255:0],m_axi_hbm19_rresp[1:0],m_axi_hbm19_rlast,m_axi_hbm19_rvalid,m_axi_hbm19_rready,m_axi_hbm20_awid[5:0],m_axi_hbm20_awaddr[32:0],m_axi_hbm20_awlen[3:0],m_axi_hbm20_awsize[2:0],m_axi_hbm20_awburst[1:0],m_axi_hbm20_awlock[1:0],m_axi_hbm20_awcache[3:0],m_axi_hbm20_awprot[2:0],m_axi_hbm20_awqos[3:0],m_axi_hbm20_awregion[3:0],m_axi_hbm20_awvalid,m_axi_hbm20_awready,m_axi_hbm20_wid[5:0],m_axi_hbm20_wdata[255:0],m_axi_hbm20_wstrb[31:0],m_axi_hbm20_wlast,m_axi_hbm20_wvalid,m_axi_hbm20_wready,m_axi_hbm20_bid[5:0],m_axi_hbm20_bresp[1:0],m_axi_hbm20_bvalid,m_axi_hbm20_bready,m_axi_hbm20_arid[5:0],m_axi_hbm20_araddr[32:0],m_axi_hbm20_arlen[3:0],m_axi_hbm20_arsize[2:0],m_axi_hbm20_arburst[1:0],m_axi_hbm20_arlock[1:0],m_axi_hbm20_arcache[3:0],m_axi_hbm20_arprot[2:0],m_axi_hbm20_arqos[3:0],m_axi_hbm20_arregion[3:0],m_axi_hbm20_arvalid,m_axi_hbm20_arready,m_axi_hbm20_rid[5:0],m_axi_hbm20_rdata[255:0],m_axi_hbm20_rresp[1:0],m_axi_hbm20_rlast,m_axi_hbm20_rvalid,m_axi_hbm20_rready,m_axi_hbm21_awid[5:0],m_axi_hbm21_awaddr[32:0],m_axi_hbm21_awlen[3:0],m_axi_hbm21_awsize[2:0],m_axi_hbm21_awburst[1:0],m_axi_hbm21_awlock[1:0],m_axi_hbm21_awcache[3:0],m_axi_hbm21_awprot[2:0],m_axi_hbm21_awqos[3:0],m_axi_hbm21_awregion[3:0],m_axi_hbm21_awvalid,m_axi_hbm21_awready,m_axi_hbm21_wid[5:0],m_axi_hbm21_wdata[255:0],m_axi_hbm21_wstrb[31:0],m_axi_hbm21_wlast,m_axi_hbm21_wvalid,m_axi_hbm21_wready,m_axi_hbm21_bid[5:0],m_axi_hbm21_bresp[1:0],m_axi_hbm21_bvalid,m_axi_hbm21_bready,m_axi_hbm21_arid[5:0],m_axi_hbm21_araddr[32:0],m_axi_hbm21_arlen[3:0],m_axi_hbm21_arsize[2:0],m_axi_hbm21_arburst[1:0],m_axi_hbm21_arlock[1:0],m_axi_hbm21_arcache[3:0],m_axi_hbm21_arprot[2:0],m_axi_hbm21_arqos[3:0],m_axi_hbm21_arregion[3:0],m_axi_hbm21_arvalid,m_axi_hbm21_arready,m_axi_hbm21_rid[5:0],m_axi_hbm21_rdata[255:0],m_axi_hbm21_rresp[1:0],m_axi_hbm21_rlast,m_axi_hbm21_rvalid,m_axi_hbm21_rready,m_axi_hbm22_awid[5:0],m_axi_hbm22_awaddr[32:0],m_axi_hbm22_awlen[3:0],m_axi_hbm22_awsize[2:0],m_axi_hbm22_awburst[1:0],m_axi_hbm22_awlock[1:0],m_axi_hbm22_awcache[3:0],m_axi_hbm22_awprot[2:0],m_axi_hbm22_awqos[3:0],m_axi_hbm22_awregion[3:0],m_axi_hbm22_awvalid,m_axi_hbm22_awready,m_axi_hbm22_wid[5:0],m_axi_hbm22_wdata[255:0],m_axi_hbm22_wstrb[31:0],m_axi_hbm22_wlast,m_axi_hbm22_wvalid,m_axi_hbm22_wready,m_axi_hbm22_bid[5:0],m_axi_hbm22_bresp[1:0],m_axi_hbm22_bvalid,m_axi_hbm22_bready,m_axi_hbm22_arid[5:0],m_axi_hbm22_araddr[32:0],m_axi_hbm22_arlen[3:0],m_axi_hbm22_arsize[2:0],m_axi_hbm22_arburst[1:0],m_axi_hbm22_arlock[1:0],m_axi_hbm22_arcache[3:0],m_axi_hbm22_arprot[2:0],m_axi_hbm22_arqos[3:0],m_axi_hbm22_arregion[3:0],m_axi_hbm22_arvalid,m_axi_hbm22_arready,m_axi_hbm22_rid[5:0],m_axi_hbm22_rdata[255:0],m_axi_hbm22_rresp[1:0],m_axi_hbm22_rlast,m_axi_hbm22_rvalid,m_axi_hbm22_rready,m_axi_hbm23_awid[5:0],m_axi_hbm23_awaddr[32:0],m_axi_hbm23_awlen[3:0],m_axi_hbm23_awsize[2:0],m_axi_hbm23_awburst[1:0],m_axi_hbm23_awlock[1:0],m_axi_hbm23_awcache[3:0],m_axi_hbm23_awprot[2:0],m_axi_hbm23_awqos[3:0],m_axi_hbm23_awregion[3:0],m_axi_hbm23_awvalid,m_axi_hbm23_awready,m_axi_hbm23_wid[5:0],m_axi_hbm23_wdata[255:0],m_axi_hbm23_wstrb[31:0],m_axi_hbm23_wlast,m_axi_hbm23_wvalid,m_axi_hbm23_wready,m_axi_hbm23_bid[5:0],m_axi_hbm23_bresp[1:0],m_axi_hbm23_bvalid,m_axi_hbm23_bready,m_axi_hbm23_arid[5:0],m_axi_hbm23_araddr[32:0],m_axi_hbm23_arlen[3:0],m_axi_hbm23_arsize[2:0],m_axi_hbm23_arburst[1:0],m_axi_hbm23_arlock[1:0],m_axi_hbm23_arcache[3:0],m_axi_hbm23_arprot[2:0],m_axi_hbm23_arqos[3:0],m_axi_hbm23_arregion[3:0],m_axi_hbm23_arvalid,m_axi_hbm23_arready,m_axi_hbm23_rid[5:0],m_axi_hbm23_rdata[255:0],m_axi_hbm23_rresp[1:0],m_axi_hbm23_rlast,m_axi_hbm23_rvalid,m_axi_hbm23_rready,m_axi_hbm24_awid[5:0],m_axi_hbm24_awaddr[32:0],m_axi_hbm24_awlen[3:0],m_axi_hbm24_awsize[2:0],m_axi_hbm24_awburst[1:0],m_axi_hbm24_awlock[1:0],m_axi_hbm24_awcache[3:0],m_axi_hbm24_awprot[2:0],m_axi_hbm24_awqos[3:0],m_axi_hbm24_awregion[3:0],m_axi_hbm24_awvalid,m_axi_hbm24_awready,m_axi_hbm24_wid[5:0],m_axi_hbm24_wdata[255:0],m_axi_hbm24_wstrb[31:0],m_axi_hbm24_wlast,m_axi_hbm24_wvalid,m_axi_hbm24_wready,m_axi_hbm24_bid[5:0],m_axi_hbm24_bresp[1:0],m_axi_hbm24_bvalid,m_axi_hbm24_bready,m_axi_hbm24_arid[5:0],m_axi_hbm24_araddr[32:0],m_axi_hbm24_arlen[3:0],m_axi_hbm24_arsize[2:0],m_axi_hbm24_arburst[1:0],m_axi_hbm24_arlock[1:0],m_axi_hbm24_arcache[3:0],m_axi_hbm24_arprot[2:0],m_axi_hbm24_arqos[3:0],m_axi_hbm24_arregion[3:0],m_axi_hbm24_arvalid,m_axi_hbm24_arready,m_axi_hbm24_rid[5:0],m_axi_hbm24_rdata[255:0],m_axi_hbm24_rresp[1:0],m_axi_hbm24_rlast,m_axi_hbm24_rvalid,m_axi_hbm24_rready,m_axi_hbm25_awid[5:0],m_axi_hbm25_awaddr[32:0],m_axi_hbm25_awlen[3:0],m_axi_hbm25_awsize[2:0],m_axi_hbm25_awburst[1:0],m_axi_hbm25_awlock[1:0],m_axi_hbm25_awcache[3:0],m_axi_hbm25_awprot[2:0],m_axi_hbm25_awqos[3:0],m_axi_hbm25_awregion[3:0],m_axi_hbm25_awvalid,m_axi_hbm25_awready,m_axi_hbm25_wid[5:0],m_axi_hbm25_wdata[255:0],m_axi_hbm25_wstrb[31:0],m_axi_hbm25_wlast,m_axi_hbm25_wvalid,m_axi_hbm25_wready,m_axi_hbm25_bid[5:0],m_axi_hbm25_bresp[1:0],m_axi_hbm25_bvalid,m_axi_hbm25_bready,m_axi_hbm25_arid[5:0],m_axi_hbm25_araddr[32:0],m_axi_hbm25_arlen[3:0],m_axi_hbm25_arsize[2:0],m_axi_hbm25_arburst[1:0],m_axi_hbm25_arlock[1:0],m_axi_hbm25_arcache[3:0],m_axi_hbm25_arprot[2:0],m_axi_hbm25_arqos[3:0],m_axi_hbm25_arregion[3:0],m_axi_hbm25_arvalid,m_axi_hbm25_arready,m_axi_hbm25_rid[5:0],m_axi_hbm25_rdata[255:0],m_axi_hbm25_rresp[1:0],m_axi_hbm25_rlast,m_axi_hbm25_rvalid,m_axi_hbm25_rready,m_axi_hbm26_awid[5:0],m_axi_hbm26_awaddr[32:0],m_axi_hbm26_awlen[3:0],m_axi_hbm26_awsize[2:0],m_axi_hbm26_awburst[1:0],m_axi_hbm26_awlock[1:0],m_axi_hbm26_awcache[3:0],m_axi_hbm26_awprot[2:0],m_axi_hbm26_awqos[3:0],m_axi_hbm26_awregion[3:0],m_axi_hbm26_awvalid,m_axi_hbm26_awready,m_axi_hbm26_wid[5:0],m_axi_hbm26_wdata[255:0],m_axi_hbm26_wstrb[31:0],m_axi_hbm26_wlast,m_axi_hbm26_wvalid,m_axi_hbm26_wready,m_axi_hbm26_bid[5:0],m_axi_hbm26_bresp[1:0],m_axi_hbm26_bvalid,m_axi_hbm26_bready,m_axi_hbm26_arid[5:0],m_axi_hbm26_araddr[32:0],m_axi_hbm26_arlen[3:0],m_axi_hbm26_arsize[2:0],m_axi_hbm26_arburst[1:0],m_axi_hbm26_arlock[1:0],m_axi_hbm26_arcache[3:0],m_axi_hbm26_arprot[2:0],m_axi_hbm26_arqos[3:0],m_axi_hbm26_arregion[3:0],m_axi_hbm26_arvalid,m_axi_hbm26_arready,m_axi_hbm26_rid[5:0],m_axi_hbm26_rdata[255:0],m_axi_hbm26_rresp[1:0],m_axi_hbm26_rlast,m_axi_hbm26_rvalid,m_axi_hbm26_rready,m_axi_hbm27_awid[5:0],m_axi_hbm27_awaddr[32:0],m_axi_hbm27_awlen[3:0],m_axi_hbm27_awsize[2:0],m_axi_hbm27_awburst[1:0],m_axi_hbm27_awlock[1:0],m_axi_hbm27_awcache[3:0],m_axi_hbm27_awprot[2:0],m_axi_hbm27_awqos[3:0],m_axi_hbm27_awregion[3:0],m_axi_hbm27_awvalid,m_axi_hbm27_awready,m_axi_hbm27_wid[5:0],m_axi_hbm27_wdata[255:0],m_axi_hbm27_wstrb[31:0],m_axi_hbm27_wlast,m_axi_hbm27_wvalid,m_axi_hbm27_wready,m_axi_hbm27_bid[5:0],m_axi_hbm27_bresp[1:0],m_axi_hbm27_bvalid,m_axi_hbm27_bready,m_axi_hbm27_arid[5:0],m_axi_hbm27_araddr[32:0],m_axi_hbm27_arlen[3:0],m_axi_hbm27_arsize[2:0],m_axi_hbm27_arburst[1:0],m_axi_hbm27_arlock[1:0],m_axi_hbm27_arcache[3:0],m_axi_hbm27_arprot[2:0],m_axi_hbm27_arqos[3:0],m_axi_hbm27_arregion[3:0],m_axi_hbm27_arvalid,m_axi_hbm27_arready,m_axi_hbm27_rid[5:0],m_axi_hbm27_rdata[255:0],m_axi_hbm27_rresp[1:0],m_axi_hbm27_rlast,m_axi_hbm27_rvalid,m_axi_hbm27_rready,m_axi_hbm28_awid[5:0],m_axi_hbm28_awaddr[32:0],m_axi_hbm28_awlen[3:0],m_axi_hbm28_awsize[2:0],m_axi_hbm28_awburst[1:0],m_axi_hbm28_awlock[1:0],m_axi_hbm28_awcache[3:0],m_axi_hbm28_awprot[2:0],m_axi_hbm28_awqos[3:0],m_axi_hbm28_awregion[3:0],m_axi_hbm28_awvalid,m_axi_hbm28_awready,m_axi_hbm28_wid[5:0],m_axi_hbm28_wdata[255:0],m_axi_hbm28_wstrb[31:0],m_axi_hbm28_wlast,m_axi_hbm28_wvalid,m_axi_hbm28_wready,m_axi_hbm28_bid[5:0],m_axi_hbm28_bresp[1:0],m_axi_hbm28_bvalid,m_axi_hbm28_bready,m_axi_hbm28_arid[5:0],m_axi_hbm28_araddr[32:0],m_axi_hbm28_arlen[3:0],m_axi_hbm28_arsize[2:0],m_axi_hbm28_arburst[1:0],m_axi_hbm28_arlock[1:0],m_axi_hbm28_arcache[3:0],m_axi_hbm28_arprot[2:0],m_axi_hbm28_arqos[3:0],m_axi_hbm28_arregion[3:0],m_axi_hbm28_arvalid,m_axi_hbm28_arready,m_axi_hbm28_rid[5:0],m_axi_hbm28_rdata[255:0],m_axi_hbm28_rresp[1:0],m_axi_hbm28_rlast,m_axi_hbm28_rvalid,m_axi_hbm28_rready,m_axi_hbm29_awid[5:0],m_axi_hbm29_awaddr[32:0],m_axi_hbm29_awlen[3:0],m_axi_hbm29_awsize[2:0],m_axi_hbm29_awburst[1:0],m_axi_hbm29_awlock[1:0],m_axi_hbm29_awcache[3:0],m_axi_hbm29_awprot[2:0],m_axi_hbm29_awqos[3:0],m_axi_hbm29_awregion[3:0],m_axi_hbm29_awvalid,m_axi_hbm29_awready,m_axi_hbm29_wid[5:0],m_axi_hbm29_wdata[255:0],m_axi_hbm29_wstrb[31:0],m_axi_hbm29_wlast,m_axi_hbm29_wvalid,m_axi_hbm29_wready,m_axi_hbm29_bid[5:0],m_axi_hbm29_bresp[1:0],m_axi_hbm29_bvalid,m_axi_hbm29_bready,m_axi_hbm29_arid[5:0],m_axi_hbm29_araddr[32:0],m_axi_hbm29_arlen[3:0],m_axi_hbm29_arsize[2:0],m_axi_hbm29_arburst[1:0],m_axi_hbm29_arlock[1:0],m_axi_hbm29_arcache[3:0],m_axi_hbm29_arprot[2:0],m_axi_hbm29_arqos[3:0],m_axi_hbm29_arregion[3:0],m_axi_hbm29_arvalid,m_axi_hbm29_arready,m_axi_hbm29_rid[5:0],m_axi_hbm29_rdata[255:0],m_axi_hbm29_rresp[1:0],m_axi_hbm29_rlast,m_axi_hbm29_rvalid,m_axi_hbm29_rready,m_axi_hbm30_awid[5:0],m_axi_hbm30_awaddr[32:0],m_axi_hbm30_awlen[3:0],m_axi_hbm30_awsize[2:0],m_axi_hbm30_awburst[1:0],m_axi_hbm30_awlock[1:0],m_axi_hbm30_awcache[3:0],m_axi_hbm30_awprot[2:0],m_axi_hbm30_awqos[3:0],m_axi_hbm30_awregion[3:0],m_axi_hbm30_awvalid,m_axi_hbm30_awready,m_axi_hbm30_wid[5:0],m_axi_hbm30_wdata[255:0],m_axi_hbm30_wstrb[31:0],m_axi_hbm30_wlast,m_axi_hbm30_wvalid,m_axi_hbm30_wready,m_axi_hbm30_bid[5:0],m_axi_hbm30_bresp[1:0],m_axi_hbm30_bvalid,m_axi_hbm30_bready,m_axi_hbm30_arid[5:0],m_axi_hbm30_araddr[32:0],m_axi_hbm30_arlen[3:0],m_axi_hbm30_arsize[2:0],m_axi_hbm30_arburst[1:0],m_axi_hbm30_arlock[1:0],m_axi_hbm30_arcache[3:0],m_axi_hbm30_arprot[2:0],m_axi_hbm30_arqos[3:0],m_axi_hbm30_arregion[3:0],m_axi_hbm30_arvalid,m_axi_hbm30_arready,m_axi_hbm30_rid[5:0],m_axi_hbm30_rdata[255:0],m_axi_hbm30_rresp[1:0],m_axi_hbm30_rlast,m_axi_hbm30_rvalid,m_axi_hbm30_rready,m_axi_hbm31_awid[5:0],m_axi_hbm31_awaddr[32:0],m_axi_hbm31_awlen[3:0],m_axi_hbm31_awsize[2:0],m_axi_hbm31_awburst[1:0],m_axi_hbm31_awlock[1:0],m_axi_hbm31_awcache[3:0],m_axi_hbm31_awprot[2:0],m_axi_hbm31_awqos[3:0],m_axi_hbm31_awregion[3:0],m_axi_hbm31_awvalid,m_axi_hbm31_awready,m_axi_hbm31_wid[5:0],m_axi_hbm31_wdata[255:0],m_axi_hbm31_wstrb[31:0],m_axi_hbm31_wlast,m_axi_hbm31_wvalid,m_axi_hbm31_wready,m_axi_hbm31_bid[5:0],m_axi_hbm31_bresp[1:0],m_axi_hbm31_bvalid,m_axi_hbm31_bready,m_axi_hbm31_arid[5:0],m_axi_hbm31_araddr[32:0],m_axi_hbm31_arlen[3:0],m_axi_hbm31_arsize[2:0],m_axi_hbm31_arburst[1:0],m_axi_hbm31_arlock[1:0],m_axi_hbm31_arcache[3:0],m_axi_hbm31_arprot[2:0],m_axi_hbm31_arqos[3:0],m_axi_hbm31_arregion[3:0],m_axi_hbm31_arvalid,m_axi_hbm31_arready,m_axi_hbm31_rid[5:0],m_axi_hbm31_rdata[255:0],m_axi_hbm31_rresp[1:0],m_axi_hbm31_rlast,m_axi_hbm31_rvalid,m_axi_hbm31_rready,s_axi_awaddr[0:0],s_axi_awvalid,s_axi_awready,s_axi_wdata[31:0],s_axi_wstrb[3:0],s_axi_wvalid,s_axi_wready,s_axi_bresp[1:0],s_axi_bvalid,s_axi_bready,s_axi_araddr[0:0],s_axi_arvalid,s_axi_arready,s_axi_rdata[31:0],s_axi_rvalid,s_axi_rready,s_axi_rresp[1:0],irq" */;
  input s_axis_aclk;
  input s_axis_aresetn;
  input m_axi_hbm_aclk;
  input m_axi_hbm_aresetn;
  input s_axi_aclk;
  input s_axi_aresetn;
  input cam_mem_aclk;
  input cam_mem_aresetn;
  input [119:0]user_metadata_in;
  input user_metadata_in_valid;
  output [119:0]user_metadata_out;
  output user_metadata_out_valid;
  input [36:0]user_extern_in;
  input [4:0]user_extern_in_valid;
  output [501:0]user_extern_out;
  output [4:0]user_extern_out_valid;
  output [511:0]m_axis_tdata;
  output [63:0]m_axis_tkeep;
  output m_axis_tvalid;
  input m_axis_tready;
  output [0:0]m_axis_tuser;
  output [0:0]m_axis_tid;
  output [0:0]m_axis_tdest;
  output m_axis_tlast;
  input [511:0]s_axis_tdata;
  input [63:0]s_axis_tkeep;
  input s_axis_tvalid;
  input s_axis_tlast;
  input [0:0]s_axis_tuser;
  input [0:0]s_axis_tid;
  input [0:0]s_axis_tdest;
  output s_axis_tready;
  output [5:0]m_axi_hbm00_awid;
  output [32:0]m_axi_hbm00_awaddr;
  output [3:0]m_axi_hbm00_awlen;
  output [2:0]m_axi_hbm00_awsize;
  output [1:0]m_axi_hbm00_awburst;
  output [1:0]m_axi_hbm00_awlock;
  output [3:0]m_axi_hbm00_awcache;
  output [2:0]m_axi_hbm00_awprot;
  output [3:0]m_axi_hbm00_awqos;
  output [3:0]m_axi_hbm00_awregion;
  output m_axi_hbm00_awvalid;
  input m_axi_hbm00_awready;
  output [5:0]m_axi_hbm00_wid;
  output [255:0]m_axi_hbm00_wdata;
  output [31:0]m_axi_hbm00_wstrb;
  output m_axi_hbm00_wlast;
  output m_axi_hbm00_wvalid;
  input m_axi_hbm00_wready;
  input [5:0]m_axi_hbm00_bid;
  input [1:0]m_axi_hbm00_bresp;
  input m_axi_hbm00_bvalid;
  output m_axi_hbm00_bready;
  output [5:0]m_axi_hbm00_arid;
  output [32:0]m_axi_hbm00_araddr;
  output [3:0]m_axi_hbm00_arlen;
  output [2:0]m_axi_hbm00_arsize;
  output [1:0]m_axi_hbm00_arburst;
  output [1:0]m_axi_hbm00_arlock;
  output [3:0]m_axi_hbm00_arcache;
  output [2:0]m_axi_hbm00_arprot;
  output [3:0]m_axi_hbm00_arqos;
  output [3:0]m_axi_hbm00_arregion;
  output m_axi_hbm00_arvalid;
  input m_axi_hbm00_arready;
  input [5:0]m_axi_hbm00_rid;
  input [255:0]m_axi_hbm00_rdata;
  input [1:0]m_axi_hbm00_rresp;
  input m_axi_hbm00_rlast;
  input m_axi_hbm00_rvalid;
  output m_axi_hbm00_rready;
  output [5:0]m_axi_hbm01_awid;
  output [32:0]m_axi_hbm01_awaddr;
  output [3:0]m_axi_hbm01_awlen;
  output [2:0]m_axi_hbm01_awsize;
  output [1:0]m_axi_hbm01_awburst;
  output [1:0]m_axi_hbm01_awlock;
  output [3:0]m_axi_hbm01_awcache;
  output [2:0]m_axi_hbm01_awprot;
  output [3:0]m_axi_hbm01_awqos;
  output [3:0]m_axi_hbm01_awregion;
  output m_axi_hbm01_awvalid;
  input m_axi_hbm01_awready;
  output [5:0]m_axi_hbm01_wid;
  output [255:0]m_axi_hbm01_wdata;
  output [31:0]m_axi_hbm01_wstrb;
  output m_axi_hbm01_wlast;
  output m_axi_hbm01_wvalid;
  input m_axi_hbm01_wready;
  input [5:0]m_axi_hbm01_bid;
  input [1:0]m_axi_hbm01_bresp;
  input m_axi_hbm01_bvalid;
  output m_axi_hbm01_bready;
  output [5:0]m_axi_hbm01_arid;
  output [32:0]m_axi_hbm01_araddr;
  output [3:0]m_axi_hbm01_arlen;
  output [2:0]m_axi_hbm01_arsize;
  output [1:0]m_axi_hbm01_arburst;
  output [1:0]m_axi_hbm01_arlock;
  output [3:0]m_axi_hbm01_arcache;
  output [2:0]m_axi_hbm01_arprot;
  output [3:0]m_axi_hbm01_arqos;
  output [3:0]m_axi_hbm01_arregion;
  output m_axi_hbm01_arvalid;
  input m_axi_hbm01_arready;
  input [5:0]m_axi_hbm01_rid;
  input [255:0]m_axi_hbm01_rdata;
  input [1:0]m_axi_hbm01_rresp;
  input m_axi_hbm01_rlast;
  input m_axi_hbm01_rvalid;
  output m_axi_hbm01_rready;
  output [5:0]m_axi_hbm02_awid;
  output [32:0]m_axi_hbm02_awaddr;
  output [3:0]m_axi_hbm02_awlen;
  output [2:0]m_axi_hbm02_awsize;
  output [1:0]m_axi_hbm02_awburst;
  output [1:0]m_axi_hbm02_awlock;
  output [3:0]m_axi_hbm02_awcache;
  output [2:0]m_axi_hbm02_awprot;
  output [3:0]m_axi_hbm02_awqos;
  output [3:0]m_axi_hbm02_awregion;
  output m_axi_hbm02_awvalid;
  input m_axi_hbm02_awready;
  output [5:0]m_axi_hbm02_wid;
  output [255:0]m_axi_hbm02_wdata;
  output [31:0]m_axi_hbm02_wstrb;
  output m_axi_hbm02_wlast;
  output m_axi_hbm02_wvalid;
  input m_axi_hbm02_wready;
  input [5:0]m_axi_hbm02_bid;
  input [1:0]m_axi_hbm02_bresp;
  input m_axi_hbm02_bvalid;
  output m_axi_hbm02_bready;
  output [5:0]m_axi_hbm02_arid;
  output [32:0]m_axi_hbm02_araddr;
  output [3:0]m_axi_hbm02_arlen;
  output [2:0]m_axi_hbm02_arsize;
  output [1:0]m_axi_hbm02_arburst;
  output [1:0]m_axi_hbm02_arlock;
  output [3:0]m_axi_hbm02_arcache;
  output [2:0]m_axi_hbm02_arprot;
  output [3:0]m_axi_hbm02_arqos;
  output [3:0]m_axi_hbm02_arregion;
  output m_axi_hbm02_arvalid;
  input m_axi_hbm02_arready;
  input [5:0]m_axi_hbm02_rid;
  input [255:0]m_axi_hbm02_rdata;
  input [1:0]m_axi_hbm02_rresp;
  input m_axi_hbm02_rlast;
  input m_axi_hbm02_rvalid;
  output m_axi_hbm02_rready;
  output [5:0]m_axi_hbm03_awid;
  output [32:0]m_axi_hbm03_awaddr;
  output [3:0]m_axi_hbm03_awlen;
  output [2:0]m_axi_hbm03_awsize;
  output [1:0]m_axi_hbm03_awburst;
  output [1:0]m_axi_hbm03_awlock;
  output [3:0]m_axi_hbm03_awcache;
  output [2:0]m_axi_hbm03_awprot;
  output [3:0]m_axi_hbm03_awqos;
  output [3:0]m_axi_hbm03_awregion;
  output m_axi_hbm03_awvalid;
  input m_axi_hbm03_awready;
  output [5:0]m_axi_hbm03_wid;
  output [255:0]m_axi_hbm03_wdata;
  output [31:0]m_axi_hbm03_wstrb;
  output m_axi_hbm03_wlast;
  output m_axi_hbm03_wvalid;
  input m_axi_hbm03_wready;
  input [5:0]m_axi_hbm03_bid;
  input [1:0]m_axi_hbm03_bresp;
  input m_axi_hbm03_bvalid;
  output m_axi_hbm03_bready;
  output [5:0]m_axi_hbm03_arid;
  output [32:0]m_axi_hbm03_araddr;
  output [3:0]m_axi_hbm03_arlen;
  output [2:0]m_axi_hbm03_arsize;
  output [1:0]m_axi_hbm03_arburst;
  output [1:0]m_axi_hbm03_arlock;
  output [3:0]m_axi_hbm03_arcache;
  output [2:0]m_axi_hbm03_arprot;
  output [3:0]m_axi_hbm03_arqos;
  output [3:0]m_axi_hbm03_arregion;
  output m_axi_hbm03_arvalid;
  input m_axi_hbm03_arready;
  input [5:0]m_axi_hbm03_rid;
  input [255:0]m_axi_hbm03_rdata;
  input [1:0]m_axi_hbm03_rresp;
  input m_axi_hbm03_rlast;
  input m_axi_hbm03_rvalid;
  output m_axi_hbm03_rready;
  output [5:0]m_axi_hbm04_awid;
  output [32:0]m_axi_hbm04_awaddr;
  output [3:0]m_axi_hbm04_awlen;
  output [2:0]m_axi_hbm04_awsize;
  output [1:0]m_axi_hbm04_awburst;
  output [1:0]m_axi_hbm04_awlock;
  output [3:0]m_axi_hbm04_awcache;
  output [2:0]m_axi_hbm04_awprot;
  output [3:0]m_axi_hbm04_awqos;
  output [3:0]m_axi_hbm04_awregion;
  output m_axi_hbm04_awvalid;
  input m_axi_hbm04_awready;
  output [5:0]m_axi_hbm04_wid;
  output [255:0]m_axi_hbm04_wdata;
  output [31:0]m_axi_hbm04_wstrb;
  output m_axi_hbm04_wlast;
  output m_axi_hbm04_wvalid;
  input m_axi_hbm04_wready;
  input [5:0]m_axi_hbm04_bid;
  input [1:0]m_axi_hbm04_bresp;
  input m_axi_hbm04_bvalid;
  output m_axi_hbm04_bready;
  output [5:0]m_axi_hbm04_arid;
  output [32:0]m_axi_hbm04_araddr;
  output [3:0]m_axi_hbm04_arlen;
  output [2:0]m_axi_hbm04_arsize;
  output [1:0]m_axi_hbm04_arburst;
  output [1:0]m_axi_hbm04_arlock;
  output [3:0]m_axi_hbm04_arcache;
  output [2:0]m_axi_hbm04_arprot;
  output [3:0]m_axi_hbm04_arqos;
  output [3:0]m_axi_hbm04_arregion;
  output m_axi_hbm04_arvalid;
  input m_axi_hbm04_arready;
  input [5:0]m_axi_hbm04_rid;
  input [255:0]m_axi_hbm04_rdata;
  input [1:0]m_axi_hbm04_rresp;
  input m_axi_hbm04_rlast;
  input m_axi_hbm04_rvalid;
  output m_axi_hbm04_rready;
  output [5:0]m_axi_hbm05_awid;
  output [32:0]m_axi_hbm05_awaddr;
  output [3:0]m_axi_hbm05_awlen;
  output [2:0]m_axi_hbm05_awsize;
  output [1:0]m_axi_hbm05_awburst;
  output [1:0]m_axi_hbm05_awlock;
  output [3:0]m_axi_hbm05_awcache;
  output [2:0]m_axi_hbm05_awprot;
  output [3:0]m_axi_hbm05_awqos;
  output [3:0]m_axi_hbm05_awregion;
  output m_axi_hbm05_awvalid;
  input m_axi_hbm05_awready;
  output [5:0]m_axi_hbm05_wid;
  output [255:0]m_axi_hbm05_wdata;
  output [31:0]m_axi_hbm05_wstrb;
  output m_axi_hbm05_wlast;
  output m_axi_hbm05_wvalid;
  input m_axi_hbm05_wready;
  input [5:0]m_axi_hbm05_bid;
  input [1:0]m_axi_hbm05_bresp;
  input m_axi_hbm05_bvalid;
  output m_axi_hbm05_bready;
  output [5:0]m_axi_hbm05_arid;
  output [32:0]m_axi_hbm05_araddr;
  output [3:0]m_axi_hbm05_arlen;
  output [2:0]m_axi_hbm05_arsize;
  output [1:0]m_axi_hbm05_arburst;
  output [1:0]m_axi_hbm05_arlock;
  output [3:0]m_axi_hbm05_arcache;
  output [2:0]m_axi_hbm05_arprot;
  output [3:0]m_axi_hbm05_arqos;
  output [3:0]m_axi_hbm05_arregion;
  output m_axi_hbm05_arvalid;
  input m_axi_hbm05_arready;
  input [5:0]m_axi_hbm05_rid;
  input [255:0]m_axi_hbm05_rdata;
  input [1:0]m_axi_hbm05_rresp;
  input m_axi_hbm05_rlast;
  input m_axi_hbm05_rvalid;
  output m_axi_hbm05_rready;
  output [5:0]m_axi_hbm06_awid;
  output [32:0]m_axi_hbm06_awaddr;
  output [3:0]m_axi_hbm06_awlen;
  output [2:0]m_axi_hbm06_awsize;
  output [1:0]m_axi_hbm06_awburst;
  output [1:0]m_axi_hbm06_awlock;
  output [3:0]m_axi_hbm06_awcache;
  output [2:0]m_axi_hbm06_awprot;
  output [3:0]m_axi_hbm06_awqos;
  output [3:0]m_axi_hbm06_awregion;
  output m_axi_hbm06_awvalid;
  input m_axi_hbm06_awready;
  output [5:0]m_axi_hbm06_wid;
  output [255:0]m_axi_hbm06_wdata;
  output [31:0]m_axi_hbm06_wstrb;
  output m_axi_hbm06_wlast;
  output m_axi_hbm06_wvalid;
  input m_axi_hbm06_wready;
  input [5:0]m_axi_hbm06_bid;
  input [1:0]m_axi_hbm06_bresp;
  input m_axi_hbm06_bvalid;
  output m_axi_hbm06_bready;
  output [5:0]m_axi_hbm06_arid;
  output [32:0]m_axi_hbm06_araddr;
  output [3:0]m_axi_hbm06_arlen;
  output [2:0]m_axi_hbm06_arsize;
  output [1:0]m_axi_hbm06_arburst;
  output [1:0]m_axi_hbm06_arlock;
  output [3:0]m_axi_hbm06_arcache;
  output [2:0]m_axi_hbm06_arprot;
  output [3:0]m_axi_hbm06_arqos;
  output [3:0]m_axi_hbm06_arregion;
  output m_axi_hbm06_arvalid;
  input m_axi_hbm06_arready;
  input [5:0]m_axi_hbm06_rid;
  input [255:0]m_axi_hbm06_rdata;
  input [1:0]m_axi_hbm06_rresp;
  input m_axi_hbm06_rlast;
  input m_axi_hbm06_rvalid;
  output m_axi_hbm06_rready;
  output [5:0]m_axi_hbm07_awid;
  output [32:0]m_axi_hbm07_awaddr;
  output [3:0]m_axi_hbm07_awlen;
  output [2:0]m_axi_hbm07_awsize;
  output [1:0]m_axi_hbm07_awburst;
  output [1:0]m_axi_hbm07_awlock;
  output [3:0]m_axi_hbm07_awcache;
  output [2:0]m_axi_hbm07_awprot;
  output [3:0]m_axi_hbm07_awqos;
  output [3:0]m_axi_hbm07_awregion;
  output m_axi_hbm07_awvalid;
  input m_axi_hbm07_awready;
  output [5:0]m_axi_hbm07_wid;
  output [255:0]m_axi_hbm07_wdata;
  output [31:0]m_axi_hbm07_wstrb;
  output m_axi_hbm07_wlast;
  output m_axi_hbm07_wvalid;
  input m_axi_hbm07_wready;
  input [5:0]m_axi_hbm07_bid;
  input [1:0]m_axi_hbm07_bresp;
  input m_axi_hbm07_bvalid;
  output m_axi_hbm07_bready;
  output [5:0]m_axi_hbm07_arid;
  output [32:0]m_axi_hbm07_araddr;
  output [3:0]m_axi_hbm07_arlen;
  output [2:0]m_axi_hbm07_arsize;
  output [1:0]m_axi_hbm07_arburst;
  output [1:0]m_axi_hbm07_arlock;
  output [3:0]m_axi_hbm07_arcache;
  output [2:0]m_axi_hbm07_arprot;
  output [3:0]m_axi_hbm07_arqos;
  output [3:0]m_axi_hbm07_arregion;
  output m_axi_hbm07_arvalid;
  input m_axi_hbm07_arready;
  input [5:0]m_axi_hbm07_rid;
  input [255:0]m_axi_hbm07_rdata;
  input [1:0]m_axi_hbm07_rresp;
  input m_axi_hbm07_rlast;
  input m_axi_hbm07_rvalid;
  output m_axi_hbm07_rready;
  output [5:0]m_axi_hbm08_awid;
  output [32:0]m_axi_hbm08_awaddr;
  output [3:0]m_axi_hbm08_awlen;
  output [2:0]m_axi_hbm08_awsize;
  output [1:0]m_axi_hbm08_awburst;
  output [1:0]m_axi_hbm08_awlock;
  output [3:0]m_axi_hbm08_awcache;
  output [2:0]m_axi_hbm08_awprot;
  output [3:0]m_axi_hbm08_awqos;
  output [3:0]m_axi_hbm08_awregion;
  output m_axi_hbm08_awvalid;
  input m_axi_hbm08_awready;
  output [5:0]m_axi_hbm08_wid;
  output [255:0]m_axi_hbm08_wdata;
  output [31:0]m_axi_hbm08_wstrb;
  output m_axi_hbm08_wlast;
  output m_axi_hbm08_wvalid;
  input m_axi_hbm08_wready;
  input [5:0]m_axi_hbm08_bid;
  input [1:0]m_axi_hbm08_bresp;
  input m_axi_hbm08_bvalid;
  output m_axi_hbm08_bready;
  output [5:0]m_axi_hbm08_arid;
  output [32:0]m_axi_hbm08_araddr;
  output [3:0]m_axi_hbm08_arlen;
  output [2:0]m_axi_hbm08_arsize;
  output [1:0]m_axi_hbm08_arburst;
  output [1:0]m_axi_hbm08_arlock;
  output [3:0]m_axi_hbm08_arcache;
  output [2:0]m_axi_hbm08_arprot;
  output [3:0]m_axi_hbm08_arqos;
  output [3:0]m_axi_hbm08_arregion;
  output m_axi_hbm08_arvalid;
  input m_axi_hbm08_arready;
  input [5:0]m_axi_hbm08_rid;
  input [255:0]m_axi_hbm08_rdata;
  input [1:0]m_axi_hbm08_rresp;
  input m_axi_hbm08_rlast;
  input m_axi_hbm08_rvalid;
  output m_axi_hbm08_rready;
  output [5:0]m_axi_hbm09_awid;
  output [32:0]m_axi_hbm09_awaddr;
  output [3:0]m_axi_hbm09_awlen;
  output [2:0]m_axi_hbm09_awsize;
  output [1:0]m_axi_hbm09_awburst;
  output [1:0]m_axi_hbm09_awlock;
  output [3:0]m_axi_hbm09_awcache;
  output [2:0]m_axi_hbm09_awprot;
  output [3:0]m_axi_hbm09_awqos;
  output [3:0]m_axi_hbm09_awregion;
  output m_axi_hbm09_awvalid;
  input m_axi_hbm09_awready;
  output [5:0]m_axi_hbm09_wid;
  output [255:0]m_axi_hbm09_wdata;
  output [31:0]m_axi_hbm09_wstrb;
  output m_axi_hbm09_wlast;
  output m_axi_hbm09_wvalid;
  input m_axi_hbm09_wready;
  input [5:0]m_axi_hbm09_bid;
  input [1:0]m_axi_hbm09_bresp;
  input m_axi_hbm09_bvalid;
  output m_axi_hbm09_bready;
  output [5:0]m_axi_hbm09_arid;
  output [32:0]m_axi_hbm09_araddr;
  output [3:0]m_axi_hbm09_arlen;
  output [2:0]m_axi_hbm09_arsize;
  output [1:0]m_axi_hbm09_arburst;
  output [1:0]m_axi_hbm09_arlock;
  output [3:0]m_axi_hbm09_arcache;
  output [2:0]m_axi_hbm09_arprot;
  output [3:0]m_axi_hbm09_arqos;
  output [3:0]m_axi_hbm09_arregion;
  output m_axi_hbm09_arvalid;
  input m_axi_hbm09_arready;
  input [5:0]m_axi_hbm09_rid;
  input [255:0]m_axi_hbm09_rdata;
  input [1:0]m_axi_hbm09_rresp;
  input m_axi_hbm09_rlast;
  input m_axi_hbm09_rvalid;
  output m_axi_hbm09_rready;
  output [5:0]m_axi_hbm10_awid;
  output [32:0]m_axi_hbm10_awaddr;
  output [3:0]m_axi_hbm10_awlen;
  output [2:0]m_axi_hbm10_awsize;
  output [1:0]m_axi_hbm10_awburst;
  output [1:0]m_axi_hbm10_awlock;
  output [3:0]m_axi_hbm10_awcache;
  output [2:0]m_axi_hbm10_awprot;
  output [3:0]m_axi_hbm10_awqos;
  output [3:0]m_axi_hbm10_awregion;
  output m_axi_hbm10_awvalid;
  input m_axi_hbm10_awready;
  output [5:0]m_axi_hbm10_wid;
  output [255:0]m_axi_hbm10_wdata;
  output [31:0]m_axi_hbm10_wstrb;
  output m_axi_hbm10_wlast;
  output m_axi_hbm10_wvalid;
  input m_axi_hbm10_wready;
  input [5:0]m_axi_hbm10_bid;
  input [1:0]m_axi_hbm10_bresp;
  input m_axi_hbm10_bvalid;
  output m_axi_hbm10_bready;
  output [5:0]m_axi_hbm10_arid;
  output [32:0]m_axi_hbm10_araddr;
  output [3:0]m_axi_hbm10_arlen;
  output [2:0]m_axi_hbm10_arsize;
  output [1:0]m_axi_hbm10_arburst;
  output [1:0]m_axi_hbm10_arlock;
  output [3:0]m_axi_hbm10_arcache;
  output [2:0]m_axi_hbm10_arprot;
  output [3:0]m_axi_hbm10_arqos;
  output [3:0]m_axi_hbm10_arregion;
  output m_axi_hbm10_arvalid;
  input m_axi_hbm10_arready;
  input [5:0]m_axi_hbm10_rid;
  input [255:0]m_axi_hbm10_rdata;
  input [1:0]m_axi_hbm10_rresp;
  input m_axi_hbm10_rlast;
  input m_axi_hbm10_rvalid;
  output m_axi_hbm10_rready;
  output [5:0]m_axi_hbm11_awid;
  output [32:0]m_axi_hbm11_awaddr;
  output [3:0]m_axi_hbm11_awlen;
  output [2:0]m_axi_hbm11_awsize;
  output [1:0]m_axi_hbm11_awburst;
  output [1:0]m_axi_hbm11_awlock;
  output [3:0]m_axi_hbm11_awcache;
  output [2:0]m_axi_hbm11_awprot;
  output [3:0]m_axi_hbm11_awqos;
  output [3:0]m_axi_hbm11_awregion;
  output m_axi_hbm11_awvalid;
  input m_axi_hbm11_awready;
  output [5:0]m_axi_hbm11_wid;
  output [255:0]m_axi_hbm11_wdata;
  output [31:0]m_axi_hbm11_wstrb;
  output m_axi_hbm11_wlast;
  output m_axi_hbm11_wvalid;
  input m_axi_hbm11_wready;
  input [5:0]m_axi_hbm11_bid;
  input [1:0]m_axi_hbm11_bresp;
  input m_axi_hbm11_bvalid;
  output m_axi_hbm11_bready;
  output [5:0]m_axi_hbm11_arid;
  output [32:0]m_axi_hbm11_araddr;
  output [3:0]m_axi_hbm11_arlen;
  output [2:0]m_axi_hbm11_arsize;
  output [1:0]m_axi_hbm11_arburst;
  output [1:0]m_axi_hbm11_arlock;
  output [3:0]m_axi_hbm11_arcache;
  output [2:0]m_axi_hbm11_arprot;
  output [3:0]m_axi_hbm11_arqos;
  output [3:0]m_axi_hbm11_arregion;
  output m_axi_hbm11_arvalid;
  input m_axi_hbm11_arready;
  input [5:0]m_axi_hbm11_rid;
  input [255:0]m_axi_hbm11_rdata;
  input [1:0]m_axi_hbm11_rresp;
  input m_axi_hbm11_rlast;
  input m_axi_hbm11_rvalid;
  output m_axi_hbm11_rready;
  output [5:0]m_axi_hbm12_awid;
  output [32:0]m_axi_hbm12_awaddr;
  output [3:0]m_axi_hbm12_awlen;
  output [2:0]m_axi_hbm12_awsize;
  output [1:0]m_axi_hbm12_awburst;
  output [1:0]m_axi_hbm12_awlock;
  output [3:0]m_axi_hbm12_awcache;
  output [2:0]m_axi_hbm12_awprot;
  output [3:0]m_axi_hbm12_awqos;
  output [3:0]m_axi_hbm12_awregion;
  output m_axi_hbm12_awvalid;
  input m_axi_hbm12_awready;
  output [5:0]m_axi_hbm12_wid;
  output [255:0]m_axi_hbm12_wdata;
  output [31:0]m_axi_hbm12_wstrb;
  output m_axi_hbm12_wlast;
  output m_axi_hbm12_wvalid;
  input m_axi_hbm12_wready;
  input [5:0]m_axi_hbm12_bid;
  input [1:0]m_axi_hbm12_bresp;
  input m_axi_hbm12_bvalid;
  output m_axi_hbm12_bready;
  output [5:0]m_axi_hbm12_arid;
  output [32:0]m_axi_hbm12_araddr;
  output [3:0]m_axi_hbm12_arlen;
  output [2:0]m_axi_hbm12_arsize;
  output [1:0]m_axi_hbm12_arburst;
  output [1:0]m_axi_hbm12_arlock;
  output [3:0]m_axi_hbm12_arcache;
  output [2:0]m_axi_hbm12_arprot;
  output [3:0]m_axi_hbm12_arqos;
  output [3:0]m_axi_hbm12_arregion;
  output m_axi_hbm12_arvalid;
  input m_axi_hbm12_arready;
  input [5:0]m_axi_hbm12_rid;
  input [255:0]m_axi_hbm12_rdata;
  input [1:0]m_axi_hbm12_rresp;
  input m_axi_hbm12_rlast;
  input m_axi_hbm12_rvalid;
  output m_axi_hbm12_rready;
  output [5:0]m_axi_hbm13_awid;
  output [32:0]m_axi_hbm13_awaddr;
  output [3:0]m_axi_hbm13_awlen;
  output [2:0]m_axi_hbm13_awsize;
  output [1:0]m_axi_hbm13_awburst;
  output [1:0]m_axi_hbm13_awlock;
  output [3:0]m_axi_hbm13_awcache;
  output [2:0]m_axi_hbm13_awprot;
  output [3:0]m_axi_hbm13_awqos;
  output [3:0]m_axi_hbm13_awregion;
  output m_axi_hbm13_awvalid;
  input m_axi_hbm13_awready;
  output [5:0]m_axi_hbm13_wid;
  output [255:0]m_axi_hbm13_wdata;
  output [31:0]m_axi_hbm13_wstrb;
  output m_axi_hbm13_wlast;
  output m_axi_hbm13_wvalid;
  input m_axi_hbm13_wready;
  input [5:0]m_axi_hbm13_bid;
  input [1:0]m_axi_hbm13_bresp;
  input m_axi_hbm13_bvalid;
  output m_axi_hbm13_bready;
  output [5:0]m_axi_hbm13_arid;
  output [32:0]m_axi_hbm13_araddr;
  output [3:0]m_axi_hbm13_arlen;
  output [2:0]m_axi_hbm13_arsize;
  output [1:0]m_axi_hbm13_arburst;
  output [1:0]m_axi_hbm13_arlock;
  output [3:0]m_axi_hbm13_arcache;
  output [2:0]m_axi_hbm13_arprot;
  output [3:0]m_axi_hbm13_arqos;
  output [3:0]m_axi_hbm13_arregion;
  output m_axi_hbm13_arvalid;
  input m_axi_hbm13_arready;
  input [5:0]m_axi_hbm13_rid;
  input [255:0]m_axi_hbm13_rdata;
  input [1:0]m_axi_hbm13_rresp;
  input m_axi_hbm13_rlast;
  input m_axi_hbm13_rvalid;
  output m_axi_hbm13_rready;
  output [5:0]m_axi_hbm14_awid;
  output [32:0]m_axi_hbm14_awaddr;
  output [3:0]m_axi_hbm14_awlen;
  output [2:0]m_axi_hbm14_awsize;
  output [1:0]m_axi_hbm14_awburst;
  output [1:0]m_axi_hbm14_awlock;
  output [3:0]m_axi_hbm14_awcache;
  output [2:0]m_axi_hbm14_awprot;
  output [3:0]m_axi_hbm14_awqos;
  output [3:0]m_axi_hbm14_awregion;
  output m_axi_hbm14_awvalid;
  input m_axi_hbm14_awready;
  output [5:0]m_axi_hbm14_wid;
  output [255:0]m_axi_hbm14_wdata;
  output [31:0]m_axi_hbm14_wstrb;
  output m_axi_hbm14_wlast;
  output m_axi_hbm14_wvalid;
  input m_axi_hbm14_wready;
  input [5:0]m_axi_hbm14_bid;
  input [1:0]m_axi_hbm14_bresp;
  input m_axi_hbm14_bvalid;
  output m_axi_hbm14_bready;
  output [5:0]m_axi_hbm14_arid;
  output [32:0]m_axi_hbm14_araddr;
  output [3:0]m_axi_hbm14_arlen;
  output [2:0]m_axi_hbm14_arsize;
  output [1:0]m_axi_hbm14_arburst;
  output [1:0]m_axi_hbm14_arlock;
  output [3:0]m_axi_hbm14_arcache;
  output [2:0]m_axi_hbm14_arprot;
  output [3:0]m_axi_hbm14_arqos;
  output [3:0]m_axi_hbm14_arregion;
  output m_axi_hbm14_arvalid;
  input m_axi_hbm14_arready;
  input [5:0]m_axi_hbm14_rid;
  input [255:0]m_axi_hbm14_rdata;
  input [1:0]m_axi_hbm14_rresp;
  input m_axi_hbm14_rlast;
  input m_axi_hbm14_rvalid;
  output m_axi_hbm14_rready;
  output [5:0]m_axi_hbm15_awid;
  output [32:0]m_axi_hbm15_awaddr;
  output [3:0]m_axi_hbm15_awlen;
  output [2:0]m_axi_hbm15_awsize;
  output [1:0]m_axi_hbm15_awburst;
  output [1:0]m_axi_hbm15_awlock;
  output [3:0]m_axi_hbm15_awcache;
  output [2:0]m_axi_hbm15_awprot;
  output [3:0]m_axi_hbm15_awqos;
  output [3:0]m_axi_hbm15_awregion;
  output m_axi_hbm15_awvalid;
  input m_axi_hbm15_awready;
  output [5:0]m_axi_hbm15_wid;
  output [255:0]m_axi_hbm15_wdata;
  output [31:0]m_axi_hbm15_wstrb;
  output m_axi_hbm15_wlast;
  output m_axi_hbm15_wvalid;
  input m_axi_hbm15_wready;
  input [5:0]m_axi_hbm15_bid;
  input [1:0]m_axi_hbm15_bresp;
  input m_axi_hbm15_bvalid;
  output m_axi_hbm15_bready;
  output [5:0]m_axi_hbm15_arid;
  output [32:0]m_axi_hbm15_araddr;
  output [3:0]m_axi_hbm15_arlen;
  output [2:0]m_axi_hbm15_arsize;
  output [1:0]m_axi_hbm15_arburst;
  output [1:0]m_axi_hbm15_arlock;
  output [3:0]m_axi_hbm15_arcache;
  output [2:0]m_axi_hbm15_arprot;
  output [3:0]m_axi_hbm15_arqos;
  output [3:0]m_axi_hbm15_arregion;
  output m_axi_hbm15_arvalid;
  input m_axi_hbm15_arready;
  input [5:0]m_axi_hbm15_rid;
  input [255:0]m_axi_hbm15_rdata;
  input [1:0]m_axi_hbm15_rresp;
  input m_axi_hbm15_rlast;
  input m_axi_hbm15_rvalid;
  output m_axi_hbm15_rready;
  output [5:0]m_axi_hbm16_awid;
  output [32:0]m_axi_hbm16_awaddr;
  output [3:0]m_axi_hbm16_awlen;
  output [2:0]m_axi_hbm16_awsize;
  output [1:0]m_axi_hbm16_awburst;
  output [1:0]m_axi_hbm16_awlock;
  output [3:0]m_axi_hbm16_awcache;
  output [2:0]m_axi_hbm16_awprot;
  output [3:0]m_axi_hbm16_awqos;
  output [3:0]m_axi_hbm16_awregion;
  output m_axi_hbm16_awvalid;
  input m_axi_hbm16_awready;
  output [5:0]m_axi_hbm16_wid;
  output [255:0]m_axi_hbm16_wdata;
  output [31:0]m_axi_hbm16_wstrb;
  output m_axi_hbm16_wlast;
  output m_axi_hbm16_wvalid;
  input m_axi_hbm16_wready;
  input [5:0]m_axi_hbm16_bid;
  input [1:0]m_axi_hbm16_bresp;
  input m_axi_hbm16_bvalid;
  output m_axi_hbm16_bready;
  output [5:0]m_axi_hbm16_arid;
  output [32:0]m_axi_hbm16_araddr;
  output [3:0]m_axi_hbm16_arlen;
  output [2:0]m_axi_hbm16_arsize;
  output [1:0]m_axi_hbm16_arburst;
  output [1:0]m_axi_hbm16_arlock;
  output [3:0]m_axi_hbm16_arcache;
  output [2:0]m_axi_hbm16_arprot;
  output [3:0]m_axi_hbm16_arqos;
  output [3:0]m_axi_hbm16_arregion;
  output m_axi_hbm16_arvalid;
  input m_axi_hbm16_arready;
  input [5:0]m_axi_hbm16_rid;
  input [255:0]m_axi_hbm16_rdata;
  input [1:0]m_axi_hbm16_rresp;
  input m_axi_hbm16_rlast;
  input m_axi_hbm16_rvalid;
  output m_axi_hbm16_rready;
  output [5:0]m_axi_hbm17_awid;
  output [32:0]m_axi_hbm17_awaddr;
  output [3:0]m_axi_hbm17_awlen;
  output [2:0]m_axi_hbm17_awsize;
  output [1:0]m_axi_hbm17_awburst;
  output [1:0]m_axi_hbm17_awlock;
  output [3:0]m_axi_hbm17_awcache;
  output [2:0]m_axi_hbm17_awprot;
  output [3:0]m_axi_hbm17_awqos;
  output [3:0]m_axi_hbm17_awregion;
  output m_axi_hbm17_awvalid;
  input m_axi_hbm17_awready;
  output [5:0]m_axi_hbm17_wid;
  output [255:0]m_axi_hbm17_wdata;
  output [31:0]m_axi_hbm17_wstrb;
  output m_axi_hbm17_wlast;
  output m_axi_hbm17_wvalid;
  input m_axi_hbm17_wready;
  input [5:0]m_axi_hbm17_bid;
  input [1:0]m_axi_hbm17_bresp;
  input m_axi_hbm17_bvalid;
  output m_axi_hbm17_bready;
  output [5:0]m_axi_hbm17_arid;
  output [32:0]m_axi_hbm17_araddr;
  output [3:0]m_axi_hbm17_arlen;
  output [2:0]m_axi_hbm17_arsize;
  output [1:0]m_axi_hbm17_arburst;
  output [1:0]m_axi_hbm17_arlock;
  output [3:0]m_axi_hbm17_arcache;
  output [2:0]m_axi_hbm17_arprot;
  output [3:0]m_axi_hbm17_arqos;
  output [3:0]m_axi_hbm17_arregion;
  output m_axi_hbm17_arvalid;
  input m_axi_hbm17_arready;
  input [5:0]m_axi_hbm17_rid;
  input [255:0]m_axi_hbm17_rdata;
  input [1:0]m_axi_hbm17_rresp;
  input m_axi_hbm17_rlast;
  input m_axi_hbm17_rvalid;
  output m_axi_hbm17_rready;
  output [5:0]m_axi_hbm18_awid;
  output [32:0]m_axi_hbm18_awaddr;
  output [3:0]m_axi_hbm18_awlen;
  output [2:0]m_axi_hbm18_awsize;
  output [1:0]m_axi_hbm18_awburst;
  output [1:0]m_axi_hbm18_awlock;
  output [3:0]m_axi_hbm18_awcache;
  output [2:0]m_axi_hbm18_awprot;
  output [3:0]m_axi_hbm18_awqos;
  output [3:0]m_axi_hbm18_awregion;
  output m_axi_hbm18_awvalid;
  input m_axi_hbm18_awready;
  output [5:0]m_axi_hbm18_wid;
  output [255:0]m_axi_hbm18_wdata;
  output [31:0]m_axi_hbm18_wstrb;
  output m_axi_hbm18_wlast;
  output m_axi_hbm18_wvalid;
  input m_axi_hbm18_wready;
  input [5:0]m_axi_hbm18_bid;
  input [1:0]m_axi_hbm18_bresp;
  input m_axi_hbm18_bvalid;
  output m_axi_hbm18_bready;
  output [5:0]m_axi_hbm18_arid;
  output [32:0]m_axi_hbm18_araddr;
  output [3:0]m_axi_hbm18_arlen;
  output [2:0]m_axi_hbm18_arsize;
  output [1:0]m_axi_hbm18_arburst;
  output [1:0]m_axi_hbm18_arlock;
  output [3:0]m_axi_hbm18_arcache;
  output [2:0]m_axi_hbm18_arprot;
  output [3:0]m_axi_hbm18_arqos;
  output [3:0]m_axi_hbm18_arregion;
  output m_axi_hbm18_arvalid;
  input m_axi_hbm18_arready;
  input [5:0]m_axi_hbm18_rid;
  input [255:0]m_axi_hbm18_rdata;
  input [1:0]m_axi_hbm18_rresp;
  input m_axi_hbm18_rlast;
  input m_axi_hbm18_rvalid;
  output m_axi_hbm18_rready;
  output [5:0]m_axi_hbm19_awid;
  output [32:0]m_axi_hbm19_awaddr;
  output [3:0]m_axi_hbm19_awlen;
  output [2:0]m_axi_hbm19_awsize;
  output [1:0]m_axi_hbm19_awburst;
  output [1:0]m_axi_hbm19_awlock;
  output [3:0]m_axi_hbm19_awcache;
  output [2:0]m_axi_hbm19_awprot;
  output [3:0]m_axi_hbm19_awqos;
  output [3:0]m_axi_hbm19_awregion;
  output m_axi_hbm19_awvalid;
  input m_axi_hbm19_awready;
  output [5:0]m_axi_hbm19_wid;
  output [255:0]m_axi_hbm19_wdata;
  output [31:0]m_axi_hbm19_wstrb;
  output m_axi_hbm19_wlast;
  output m_axi_hbm19_wvalid;
  input m_axi_hbm19_wready;
  input [5:0]m_axi_hbm19_bid;
  input [1:0]m_axi_hbm19_bresp;
  input m_axi_hbm19_bvalid;
  output m_axi_hbm19_bready;
  output [5:0]m_axi_hbm19_arid;
  output [32:0]m_axi_hbm19_araddr;
  output [3:0]m_axi_hbm19_arlen;
  output [2:0]m_axi_hbm19_arsize;
  output [1:0]m_axi_hbm19_arburst;
  output [1:0]m_axi_hbm19_arlock;
  output [3:0]m_axi_hbm19_arcache;
  output [2:0]m_axi_hbm19_arprot;
  output [3:0]m_axi_hbm19_arqos;
  output [3:0]m_axi_hbm19_arregion;
  output m_axi_hbm19_arvalid;
  input m_axi_hbm19_arready;
  input [5:0]m_axi_hbm19_rid;
  input [255:0]m_axi_hbm19_rdata;
  input [1:0]m_axi_hbm19_rresp;
  input m_axi_hbm19_rlast;
  input m_axi_hbm19_rvalid;
  output m_axi_hbm19_rready;
  output [5:0]m_axi_hbm20_awid;
  output [32:0]m_axi_hbm20_awaddr;
  output [3:0]m_axi_hbm20_awlen;
  output [2:0]m_axi_hbm20_awsize;
  output [1:0]m_axi_hbm20_awburst;
  output [1:0]m_axi_hbm20_awlock;
  output [3:0]m_axi_hbm20_awcache;
  output [2:0]m_axi_hbm20_awprot;
  output [3:0]m_axi_hbm20_awqos;
  output [3:0]m_axi_hbm20_awregion;
  output m_axi_hbm20_awvalid;
  input m_axi_hbm20_awready;
  output [5:0]m_axi_hbm20_wid;
  output [255:0]m_axi_hbm20_wdata;
  output [31:0]m_axi_hbm20_wstrb;
  output m_axi_hbm20_wlast;
  output m_axi_hbm20_wvalid;
  input m_axi_hbm20_wready;
  input [5:0]m_axi_hbm20_bid;
  input [1:0]m_axi_hbm20_bresp;
  input m_axi_hbm20_bvalid;
  output m_axi_hbm20_bready;
  output [5:0]m_axi_hbm20_arid;
  output [32:0]m_axi_hbm20_araddr;
  output [3:0]m_axi_hbm20_arlen;
  output [2:0]m_axi_hbm20_arsize;
  output [1:0]m_axi_hbm20_arburst;
  output [1:0]m_axi_hbm20_arlock;
  output [3:0]m_axi_hbm20_arcache;
  output [2:0]m_axi_hbm20_arprot;
  output [3:0]m_axi_hbm20_arqos;
  output [3:0]m_axi_hbm20_arregion;
  output m_axi_hbm20_arvalid;
  input m_axi_hbm20_arready;
  input [5:0]m_axi_hbm20_rid;
  input [255:0]m_axi_hbm20_rdata;
  input [1:0]m_axi_hbm20_rresp;
  input m_axi_hbm20_rlast;
  input m_axi_hbm20_rvalid;
  output m_axi_hbm20_rready;
  output [5:0]m_axi_hbm21_awid;
  output [32:0]m_axi_hbm21_awaddr;
  output [3:0]m_axi_hbm21_awlen;
  output [2:0]m_axi_hbm21_awsize;
  output [1:0]m_axi_hbm21_awburst;
  output [1:0]m_axi_hbm21_awlock;
  output [3:0]m_axi_hbm21_awcache;
  output [2:0]m_axi_hbm21_awprot;
  output [3:0]m_axi_hbm21_awqos;
  output [3:0]m_axi_hbm21_awregion;
  output m_axi_hbm21_awvalid;
  input m_axi_hbm21_awready;
  output [5:0]m_axi_hbm21_wid;
  output [255:0]m_axi_hbm21_wdata;
  output [31:0]m_axi_hbm21_wstrb;
  output m_axi_hbm21_wlast;
  output m_axi_hbm21_wvalid;
  input m_axi_hbm21_wready;
  input [5:0]m_axi_hbm21_bid;
  input [1:0]m_axi_hbm21_bresp;
  input m_axi_hbm21_bvalid;
  output m_axi_hbm21_bready;
  output [5:0]m_axi_hbm21_arid;
  output [32:0]m_axi_hbm21_araddr;
  output [3:0]m_axi_hbm21_arlen;
  output [2:0]m_axi_hbm21_arsize;
  output [1:0]m_axi_hbm21_arburst;
  output [1:0]m_axi_hbm21_arlock;
  output [3:0]m_axi_hbm21_arcache;
  output [2:0]m_axi_hbm21_arprot;
  output [3:0]m_axi_hbm21_arqos;
  output [3:0]m_axi_hbm21_arregion;
  output m_axi_hbm21_arvalid;
  input m_axi_hbm21_arready;
  input [5:0]m_axi_hbm21_rid;
  input [255:0]m_axi_hbm21_rdata;
  input [1:0]m_axi_hbm21_rresp;
  input m_axi_hbm21_rlast;
  input m_axi_hbm21_rvalid;
  output m_axi_hbm21_rready;
  output [5:0]m_axi_hbm22_awid;
  output [32:0]m_axi_hbm22_awaddr;
  output [3:0]m_axi_hbm22_awlen;
  output [2:0]m_axi_hbm22_awsize;
  output [1:0]m_axi_hbm22_awburst;
  output [1:0]m_axi_hbm22_awlock;
  output [3:0]m_axi_hbm22_awcache;
  output [2:0]m_axi_hbm22_awprot;
  output [3:0]m_axi_hbm22_awqos;
  output [3:0]m_axi_hbm22_awregion;
  output m_axi_hbm22_awvalid;
  input m_axi_hbm22_awready;
  output [5:0]m_axi_hbm22_wid;
  output [255:0]m_axi_hbm22_wdata;
  output [31:0]m_axi_hbm22_wstrb;
  output m_axi_hbm22_wlast;
  output m_axi_hbm22_wvalid;
  input m_axi_hbm22_wready;
  input [5:0]m_axi_hbm22_bid;
  input [1:0]m_axi_hbm22_bresp;
  input m_axi_hbm22_bvalid;
  output m_axi_hbm22_bready;
  output [5:0]m_axi_hbm22_arid;
  output [32:0]m_axi_hbm22_araddr;
  output [3:0]m_axi_hbm22_arlen;
  output [2:0]m_axi_hbm22_arsize;
  output [1:0]m_axi_hbm22_arburst;
  output [1:0]m_axi_hbm22_arlock;
  output [3:0]m_axi_hbm22_arcache;
  output [2:0]m_axi_hbm22_arprot;
  output [3:0]m_axi_hbm22_arqos;
  output [3:0]m_axi_hbm22_arregion;
  output m_axi_hbm22_arvalid;
  input m_axi_hbm22_arready;
  input [5:0]m_axi_hbm22_rid;
  input [255:0]m_axi_hbm22_rdata;
  input [1:0]m_axi_hbm22_rresp;
  input m_axi_hbm22_rlast;
  input m_axi_hbm22_rvalid;
  output m_axi_hbm22_rready;
  output [5:0]m_axi_hbm23_awid;
  output [32:0]m_axi_hbm23_awaddr;
  output [3:0]m_axi_hbm23_awlen;
  output [2:0]m_axi_hbm23_awsize;
  output [1:0]m_axi_hbm23_awburst;
  output [1:0]m_axi_hbm23_awlock;
  output [3:0]m_axi_hbm23_awcache;
  output [2:0]m_axi_hbm23_awprot;
  output [3:0]m_axi_hbm23_awqos;
  output [3:0]m_axi_hbm23_awregion;
  output m_axi_hbm23_awvalid;
  input m_axi_hbm23_awready;
  output [5:0]m_axi_hbm23_wid;
  output [255:0]m_axi_hbm23_wdata;
  output [31:0]m_axi_hbm23_wstrb;
  output m_axi_hbm23_wlast;
  output m_axi_hbm23_wvalid;
  input m_axi_hbm23_wready;
  input [5:0]m_axi_hbm23_bid;
  input [1:0]m_axi_hbm23_bresp;
  input m_axi_hbm23_bvalid;
  output m_axi_hbm23_bready;
  output [5:0]m_axi_hbm23_arid;
  output [32:0]m_axi_hbm23_araddr;
  output [3:0]m_axi_hbm23_arlen;
  output [2:0]m_axi_hbm23_arsize;
  output [1:0]m_axi_hbm23_arburst;
  output [1:0]m_axi_hbm23_arlock;
  output [3:0]m_axi_hbm23_arcache;
  output [2:0]m_axi_hbm23_arprot;
  output [3:0]m_axi_hbm23_arqos;
  output [3:0]m_axi_hbm23_arregion;
  output m_axi_hbm23_arvalid;
  input m_axi_hbm23_arready;
  input [5:0]m_axi_hbm23_rid;
  input [255:0]m_axi_hbm23_rdata;
  input [1:0]m_axi_hbm23_rresp;
  input m_axi_hbm23_rlast;
  input m_axi_hbm23_rvalid;
  output m_axi_hbm23_rready;
  output [5:0]m_axi_hbm24_awid;
  output [32:0]m_axi_hbm24_awaddr;
  output [3:0]m_axi_hbm24_awlen;
  output [2:0]m_axi_hbm24_awsize;
  output [1:0]m_axi_hbm24_awburst;
  output [1:0]m_axi_hbm24_awlock;
  output [3:0]m_axi_hbm24_awcache;
  output [2:0]m_axi_hbm24_awprot;
  output [3:0]m_axi_hbm24_awqos;
  output [3:0]m_axi_hbm24_awregion;
  output m_axi_hbm24_awvalid;
  input m_axi_hbm24_awready;
  output [5:0]m_axi_hbm24_wid;
  output [255:0]m_axi_hbm24_wdata;
  output [31:0]m_axi_hbm24_wstrb;
  output m_axi_hbm24_wlast;
  output m_axi_hbm24_wvalid;
  input m_axi_hbm24_wready;
  input [5:0]m_axi_hbm24_bid;
  input [1:0]m_axi_hbm24_bresp;
  input m_axi_hbm24_bvalid;
  output m_axi_hbm24_bready;
  output [5:0]m_axi_hbm24_arid;
  output [32:0]m_axi_hbm24_araddr;
  output [3:0]m_axi_hbm24_arlen;
  output [2:0]m_axi_hbm24_arsize;
  output [1:0]m_axi_hbm24_arburst;
  output [1:0]m_axi_hbm24_arlock;
  output [3:0]m_axi_hbm24_arcache;
  output [2:0]m_axi_hbm24_arprot;
  output [3:0]m_axi_hbm24_arqos;
  output [3:0]m_axi_hbm24_arregion;
  output m_axi_hbm24_arvalid;
  input m_axi_hbm24_arready;
  input [5:0]m_axi_hbm24_rid;
  input [255:0]m_axi_hbm24_rdata;
  input [1:0]m_axi_hbm24_rresp;
  input m_axi_hbm24_rlast;
  input m_axi_hbm24_rvalid;
  output m_axi_hbm24_rready;
  output [5:0]m_axi_hbm25_awid;
  output [32:0]m_axi_hbm25_awaddr;
  output [3:0]m_axi_hbm25_awlen;
  output [2:0]m_axi_hbm25_awsize;
  output [1:0]m_axi_hbm25_awburst;
  output [1:0]m_axi_hbm25_awlock;
  output [3:0]m_axi_hbm25_awcache;
  output [2:0]m_axi_hbm25_awprot;
  output [3:0]m_axi_hbm25_awqos;
  output [3:0]m_axi_hbm25_awregion;
  output m_axi_hbm25_awvalid;
  input m_axi_hbm25_awready;
  output [5:0]m_axi_hbm25_wid;
  output [255:0]m_axi_hbm25_wdata;
  output [31:0]m_axi_hbm25_wstrb;
  output m_axi_hbm25_wlast;
  output m_axi_hbm25_wvalid;
  input m_axi_hbm25_wready;
  input [5:0]m_axi_hbm25_bid;
  input [1:0]m_axi_hbm25_bresp;
  input m_axi_hbm25_bvalid;
  output m_axi_hbm25_bready;
  output [5:0]m_axi_hbm25_arid;
  output [32:0]m_axi_hbm25_araddr;
  output [3:0]m_axi_hbm25_arlen;
  output [2:0]m_axi_hbm25_arsize;
  output [1:0]m_axi_hbm25_arburst;
  output [1:0]m_axi_hbm25_arlock;
  output [3:0]m_axi_hbm25_arcache;
  output [2:0]m_axi_hbm25_arprot;
  output [3:0]m_axi_hbm25_arqos;
  output [3:0]m_axi_hbm25_arregion;
  output m_axi_hbm25_arvalid;
  input m_axi_hbm25_arready;
  input [5:0]m_axi_hbm25_rid;
  input [255:0]m_axi_hbm25_rdata;
  input [1:0]m_axi_hbm25_rresp;
  input m_axi_hbm25_rlast;
  input m_axi_hbm25_rvalid;
  output m_axi_hbm25_rready;
  output [5:0]m_axi_hbm26_awid;
  output [32:0]m_axi_hbm26_awaddr;
  output [3:0]m_axi_hbm26_awlen;
  output [2:0]m_axi_hbm26_awsize;
  output [1:0]m_axi_hbm26_awburst;
  output [1:0]m_axi_hbm26_awlock;
  output [3:0]m_axi_hbm26_awcache;
  output [2:0]m_axi_hbm26_awprot;
  output [3:0]m_axi_hbm26_awqos;
  output [3:0]m_axi_hbm26_awregion;
  output m_axi_hbm26_awvalid;
  input m_axi_hbm26_awready;
  output [5:0]m_axi_hbm26_wid;
  output [255:0]m_axi_hbm26_wdata;
  output [31:0]m_axi_hbm26_wstrb;
  output m_axi_hbm26_wlast;
  output m_axi_hbm26_wvalid;
  input m_axi_hbm26_wready;
  input [5:0]m_axi_hbm26_bid;
  input [1:0]m_axi_hbm26_bresp;
  input m_axi_hbm26_bvalid;
  output m_axi_hbm26_bready;
  output [5:0]m_axi_hbm26_arid;
  output [32:0]m_axi_hbm26_araddr;
  output [3:0]m_axi_hbm26_arlen;
  output [2:0]m_axi_hbm26_arsize;
  output [1:0]m_axi_hbm26_arburst;
  output [1:0]m_axi_hbm26_arlock;
  output [3:0]m_axi_hbm26_arcache;
  output [2:0]m_axi_hbm26_arprot;
  output [3:0]m_axi_hbm26_arqos;
  output [3:0]m_axi_hbm26_arregion;
  output m_axi_hbm26_arvalid;
  input m_axi_hbm26_arready;
  input [5:0]m_axi_hbm26_rid;
  input [255:0]m_axi_hbm26_rdata;
  input [1:0]m_axi_hbm26_rresp;
  input m_axi_hbm26_rlast;
  input m_axi_hbm26_rvalid;
  output m_axi_hbm26_rready;
  output [5:0]m_axi_hbm27_awid;
  output [32:0]m_axi_hbm27_awaddr;
  output [3:0]m_axi_hbm27_awlen;
  output [2:0]m_axi_hbm27_awsize;
  output [1:0]m_axi_hbm27_awburst;
  output [1:0]m_axi_hbm27_awlock;
  output [3:0]m_axi_hbm27_awcache;
  output [2:0]m_axi_hbm27_awprot;
  output [3:0]m_axi_hbm27_awqos;
  output [3:0]m_axi_hbm27_awregion;
  output m_axi_hbm27_awvalid;
  input m_axi_hbm27_awready;
  output [5:0]m_axi_hbm27_wid;
  output [255:0]m_axi_hbm27_wdata;
  output [31:0]m_axi_hbm27_wstrb;
  output m_axi_hbm27_wlast;
  output m_axi_hbm27_wvalid;
  input m_axi_hbm27_wready;
  input [5:0]m_axi_hbm27_bid;
  input [1:0]m_axi_hbm27_bresp;
  input m_axi_hbm27_bvalid;
  output m_axi_hbm27_bready;
  output [5:0]m_axi_hbm27_arid;
  output [32:0]m_axi_hbm27_araddr;
  output [3:0]m_axi_hbm27_arlen;
  output [2:0]m_axi_hbm27_arsize;
  output [1:0]m_axi_hbm27_arburst;
  output [1:0]m_axi_hbm27_arlock;
  output [3:0]m_axi_hbm27_arcache;
  output [2:0]m_axi_hbm27_arprot;
  output [3:0]m_axi_hbm27_arqos;
  output [3:0]m_axi_hbm27_arregion;
  output m_axi_hbm27_arvalid;
  input m_axi_hbm27_arready;
  input [5:0]m_axi_hbm27_rid;
  input [255:0]m_axi_hbm27_rdata;
  input [1:0]m_axi_hbm27_rresp;
  input m_axi_hbm27_rlast;
  input m_axi_hbm27_rvalid;
  output m_axi_hbm27_rready;
  output [5:0]m_axi_hbm28_awid;
  output [32:0]m_axi_hbm28_awaddr;
  output [3:0]m_axi_hbm28_awlen;
  output [2:0]m_axi_hbm28_awsize;
  output [1:0]m_axi_hbm28_awburst;
  output [1:0]m_axi_hbm28_awlock;
  output [3:0]m_axi_hbm28_awcache;
  output [2:0]m_axi_hbm28_awprot;
  output [3:0]m_axi_hbm28_awqos;
  output [3:0]m_axi_hbm28_awregion;
  output m_axi_hbm28_awvalid;
  input m_axi_hbm28_awready;
  output [5:0]m_axi_hbm28_wid;
  output [255:0]m_axi_hbm28_wdata;
  output [31:0]m_axi_hbm28_wstrb;
  output m_axi_hbm28_wlast;
  output m_axi_hbm28_wvalid;
  input m_axi_hbm28_wready;
  input [5:0]m_axi_hbm28_bid;
  input [1:0]m_axi_hbm28_bresp;
  input m_axi_hbm28_bvalid;
  output m_axi_hbm28_bready;
  output [5:0]m_axi_hbm28_arid;
  output [32:0]m_axi_hbm28_araddr;
  output [3:0]m_axi_hbm28_arlen;
  output [2:0]m_axi_hbm28_arsize;
  output [1:0]m_axi_hbm28_arburst;
  output [1:0]m_axi_hbm28_arlock;
  output [3:0]m_axi_hbm28_arcache;
  output [2:0]m_axi_hbm28_arprot;
  output [3:0]m_axi_hbm28_arqos;
  output [3:0]m_axi_hbm28_arregion;
  output m_axi_hbm28_arvalid;
  input m_axi_hbm28_arready;
  input [5:0]m_axi_hbm28_rid;
  input [255:0]m_axi_hbm28_rdata;
  input [1:0]m_axi_hbm28_rresp;
  input m_axi_hbm28_rlast;
  input m_axi_hbm28_rvalid;
  output m_axi_hbm28_rready;
  output [5:0]m_axi_hbm29_awid;
  output [32:0]m_axi_hbm29_awaddr;
  output [3:0]m_axi_hbm29_awlen;
  output [2:0]m_axi_hbm29_awsize;
  output [1:0]m_axi_hbm29_awburst;
  output [1:0]m_axi_hbm29_awlock;
  output [3:0]m_axi_hbm29_awcache;
  output [2:0]m_axi_hbm29_awprot;
  output [3:0]m_axi_hbm29_awqos;
  output [3:0]m_axi_hbm29_awregion;
  output m_axi_hbm29_awvalid;
  input m_axi_hbm29_awready;
  output [5:0]m_axi_hbm29_wid;
  output [255:0]m_axi_hbm29_wdata;
  output [31:0]m_axi_hbm29_wstrb;
  output m_axi_hbm29_wlast;
  output m_axi_hbm29_wvalid;
  input m_axi_hbm29_wready;
  input [5:0]m_axi_hbm29_bid;
  input [1:0]m_axi_hbm29_bresp;
  input m_axi_hbm29_bvalid;
  output m_axi_hbm29_bready;
  output [5:0]m_axi_hbm29_arid;
  output [32:0]m_axi_hbm29_araddr;
  output [3:0]m_axi_hbm29_arlen;
  output [2:0]m_axi_hbm29_arsize;
  output [1:0]m_axi_hbm29_arburst;
  output [1:0]m_axi_hbm29_arlock;
  output [3:0]m_axi_hbm29_arcache;
  output [2:0]m_axi_hbm29_arprot;
  output [3:0]m_axi_hbm29_arqos;
  output [3:0]m_axi_hbm29_arregion;
  output m_axi_hbm29_arvalid;
  input m_axi_hbm29_arready;
  input [5:0]m_axi_hbm29_rid;
  input [255:0]m_axi_hbm29_rdata;
  input [1:0]m_axi_hbm29_rresp;
  input m_axi_hbm29_rlast;
  input m_axi_hbm29_rvalid;
  output m_axi_hbm29_rready;
  output [5:0]m_axi_hbm30_awid;
  output [32:0]m_axi_hbm30_awaddr;
  output [3:0]m_axi_hbm30_awlen;
  output [2:0]m_axi_hbm30_awsize;
  output [1:0]m_axi_hbm30_awburst;
  output [1:0]m_axi_hbm30_awlock;
  output [3:0]m_axi_hbm30_awcache;
  output [2:0]m_axi_hbm30_awprot;
  output [3:0]m_axi_hbm30_awqos;
  output [3:0]m_axi_hbm30_awregion;
  output m_axi_hbm30_awvalid;
  input m_axi_hbm30_awready;
  output [5:0]m_axi_hbm30_wid;
  output [255:0]m_axi_hbm30_wdata;
  output [31:0]m_axi_hbm30_wstrb;
  output m_axi_hbm30_wlast;
  output m_axi_hbm30_wvalid;
  input m_axi_hbm30_wready;
  input [5:0]m_axi_hbm30_bid;
  input [1:0]m_axi_hbm30_bresp;
  input m_axi_hbm30_bvalid;
  output m_axi_hbm30_bready;
  output [5:0]m_axi_hbm30_arid;
  output [32:0]m_axi_hbm30_araddr;
  output [3:0]m_axi_hbm30_arlen;
  output [2:0]m_axi_hbm30_arsize;
  output [1:0]m_axi_hbm30_arburst;
  output [1:0]m_axi_hbm30_arlock;
  output [3:0]m_axi_hbm30_arcache;
  output [2:0]m_axi_hbm30_arprot;
  output [3:0]m_axi_hbm30_arqos;
  output [3:0]m_axi_hbm30_arregion;
  output m_axi_hbm30_arvalid;
  input m_axi_hbm30_arready;
  input [5:0]m_axi_hbm30_rid;
  input [255:0]m_axi_hbm30_rdata;
  input [1:0]m_axi_hbm30_rresp;
  input m_axi_hbm30_rlast;
  input m_axi_hbm30_rvalid;
  output m_axi_hbm30_rready;
  output [5:0]m_axi_hbm31_awid;
  output [32:0]m_axi_hbm31_awaddr;
  output [3:0]m_axi_hbm31_awlen;
  output [2:0]m_axi_hbm31_awsize;
  output [1:0]m_axi_hbm31_awburst;
  output [1:0]m_axi_hbm31_awlock;
  output [3:0]m_axi_hbm31_awcache;
  output [2:0]m_axi_hbm31_awprot;
  output [3:0]m_axi_hbm31_awqos;
  output [3:0]m_axi_hbm31_awregion;
  output m_axi_hbm31_awvalid;
  input m_axi_hbm31_awready;
  output [5:0]m_axi_hbm31_wid;
  output [255:0]m_axi_hbm31_wdata;
  output [31:0]m_axi_hbm31_wstrb;
  output m_axi_hbm31_wlast;
  output m_axi_hbm31_wvalid;
  input m_axi_hbm31_wready;
  input [5:0]m_axi_hbm31_bid;
  input [1:0]m_axi_hbm31_bresp;
  input m_axi_hbm31_bvalid;
  output m_axi_hbm31_bready;
  output [5:0]m_axi_hbm31_arid;
  output [32:0]m_axi_hbm31_araddr;
  output [3:0]m_axi_hbm31_arlen;
  output [2:0]m_axi_hbm31_arsize;
  output [1:0]m_axi_hbm31_arburst;
  output [1:0]m_axi_hbm31_arlock;
  output [3:0]m_axi_hbm31_arcache;
  output [2:0]m_axi_hbm31_arprot;
  output [3:0]m_axi_hbm31_arqos;
  output [3:0]m_axi_hbm31_arregion;
  output m_axi_hbm31_arvalid;
  input m_axi_hbm31_arready;
  input [5:0]m_axi_hbm31_rid;
  input [255:0]m_axi_hbm31_rdata;
  input [1:0]m_axi_hbm31_rresp;
  input m_axi_hbm31_rlast;
  input m_axi_hbm31_rvalid;
  output m_axi_hbm31_rready;
  input [0:0]s_axi_awaddr;
  input s_axi_awvalid;
  output s_axi_awready;
  input [31:0]s_axi_wdata;
  input [3:0]s_axi_wstrb;
  input s_axi_wvalid;
  output s_axi_wready;
  output [1:0]s_axi_bresp;
  output s_axi_bvalid;
  input s_axi_bready;
  input [0:0]s_axi_araddr;
  input s_axi_arvalid;
  output s_axi_arready;
  output [31:0]s_axi_rdata;
  output s_axi_rvalid;
  input s_axi_rready;
  output [1:0]s_axi_rresp;
  output irq;
endmodule
