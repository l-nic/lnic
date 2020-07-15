
module TrueDualPortRAM #(
    parameter DATA_WIDTH = 32,
    parameter NUM_ENTRIES = 2048,
    parameter ADDR_WIDTH = $clog2(NUM_ENTRIES)  
)
(
    input                   clock,
    input                   reset,

    // PortA
    input  [ADDR_WIDTH-1:0] portA_addr,
    output [DATA_WIDTH-1:0] portA_dout,
    input                   portA_we,
    input  [DATA_WIDTH-1:0] portA_din,

    // PortB
    input  [ADDR_WIDTH-1:0] portB_addr,
    output [DATA_WIDTH-1:0] portB_dout,
    input                   portB_we,
    input  [DATA_WIDTH-1:0] portB_din
);

    //  Xilinx True Dual Port RAM, No Change, Single Clock
    xilinx_true_dual_port_no_change_1_clock_ram #(
      .RAM_WIDTH(DATA_WIDTH),         // Specify RAM data width
      .RAM_DEPTH(NUM_ENTRIES),        // Specify RAM depth (number of entries)
      .RAM_PERFORMANCE("LOW_LATENCY") // Select "HIGH_PERFORMANCE" or "LOW_LATENCY"
    ) tdpram_inst (
      .addra(portA_addr),   // Port A address bus, width determined from RAM_DEPTH
      .addrb(portB_addr),   // Port B address bus, width determined from RAM_DEPTH
      .dina(portA_din),     // Port A RAM input data, width determined from RAM_WIDTH
      .dinb(portB_din),     // Port B RAM input data, width determined from RAM_WIDTH
      .clka(clock),     // Clock
      .wea(portA_we),       // Port A write enable
      .web(portB_we),       // Port B write enable
      .ena(1'b1),       // Port A RAM Enable, for additional power savings, disable port when not in use
      .enb(1'b1),       // Port B RAM Enable, for additional power savings, disable port when not in use
      .rsta(reset),     // Port A output reset (does not affect memory contents)
      .rstb(reset),     // Port B output reset (does not affect memory contents)
      .regcea(1'b1), // Port A output register enable
      .regceb(1'b1), // Port B output register enable
      .douta(portA_dout),   // Port A RAM output data, width determined from RAM_WIDTH
      .doutb(portB_dout)    // Port B RAM output data, width determined from RAM_WIDTH
    );

endmodule
