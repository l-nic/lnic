
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

    wire dbiterra;
    wire dbiterrb;
    wire sbiterra;
    wire sbiterrb;

    // xpm_memory_tdpram: True Dual Port RAM
    // Xilinx Parameterized Macro, version 2019.2
  
    xpm_memory_tdpram #(
       .ADDR_WIDTH_A(ADDR_WIDTH),              // DECIMAL
       .ADDR_WIDTH_B(ADDR_WIDTH),              // DECIMAL
       .AUTO_SLEEP_TIME(0),                    // DECIMAL
       .BYTE_WRITE_WIDTH_A(DATA_WIDTH),        // DECIMAL
       .BYTE_WRITE_WIDTH_B(DATA_WIDTH),        // DECIMAL
//       .CASCADE_HEIGHT(0),             // DECIMAL (unsupported in Vivado 2018.3)
       .CLOCKING_MODE("common_clock"), // String
       .ECC_MODE("no_ecc"),            // String
       .MEMORY_INIT_FILE("none"),      // String
       .MEMORY_INIT_PARAM("0"),        // String
       .MEMORY_OPTIMIZATION("true"),   // String
       .MEMORY_PRIMITIVE("auto"),      // String
       .MEMORY_SIZE(NUM_ENTRIES*DATA_WIDTH),             // DECIMAL
       .MESSAGE_CONTROL(0),            // DECIMAL
       .READ_DATA_WIDTH_A(DATA_WIDTH),         // DECIMAL
       .READ_DATA_WIDTH_B(DATA_WIDTH),         // DECIMAL
       .READ_LATENCY_A(1),             // DECIMAL
       .READ_LATENCY_B(1),             // DECIMAL
       .READ_RESET_VALUE_A("0"),       // String
       .READ_RESET_VALUE_B("0"),       // String
       .RST_MODE_A("SYNC"),            // String
       .RST_MODE_B("SYNC"),            // String
//       .SIM_ASSERT_CHK(0),             // DECIMAL; 0=disable simulation messages, 1=enable simulation messages
       .USE_EMBEDDED_CONSTRAINT(0),    // DECIMAL
       .USE_MEM_INIT(1),               // DECIMAL
       .WAKEUP_TIME("disable_sleep"),  // String
       .WRITE_DATA_WIDTH_A(DATA_WIDTH),        // DECIMAL
       .WRITE_DATA_WIDTH_B(DATA_WIDTH),        // DECIMAL
       .WRITE_MODE_A("no_change"),     // String
       .WRITE_MODE_B("no_change")      // String
    )
    xpm_memory_tdpram_inst (
       .dbiterra(dbiterra),             // 1-bit output: Status signal to indicate double bit error occurrence
                                        // on the data output of port A.
  
       .dbiterrb(dbiterrb),             // 1-bit output: Status signal to indicate double bit error occurrence
                                        // on the data output of port A.
  
       .douta(portA_dout),                   // READ_DATA_WIDTH_A-bit output: Data output for port A read operations.
       .doutb(portB_dout),                   // READ_DATA_WIDTH_B-bit output: Data output for port B read operations.
       .sbiterra(sbiterra),             // 1-bit output: Status signal to indicate single bit error occurrence
                                        // on the data output of port A.
  
       .sbiterrb(sbiterrb),             // 1-bit output: Status signal to indicate single bit error occurrence
                                        // on the data output of port B.
  
       .addra(portA_addr),                   // ADDR_WIDTH_A-bit input: Address for port A write and read operations.
       .addrb(portB_addr),                   // ADDR_WIDTH_B-bit input: Address for port B write and read operations.
       .clka(clock),                     // 1-bit input: Clock signal for port A. Also clocks port B when
                                        // parameter CLOCKING_MODE is "common_clock".
  
       .clkb(clock),                     // 1-bit input: Clock signal for port B when parameter CLOCKING_MODE is
                                        // "independent_clock". Unused when parameter CLOCKING_MODE is
                                        // "common_clock".
  
       .dina(portA_din),                     // WRITE_DATA_WIDTH_A-bit input: Data input for port A write operations.
       .dinb(portB_din),                     // WRITE_DATA_WIDTH_B-bit input: Data input for port B write operations.
       .ena(1'b1),                       // 1-bit input: Memory enable signal for port A. Must be high on clock
                                        // cycles when read or write operations are initiated. Pipelined
                                        // internally.
  
       .enb(1'b1),                       // 1-bit input: Memory enable signal for port B. Must be high on clock
                                        // cycles when read or write operations are initiated. Pipelined
                                        // internally.
  
       .injectdbiterra(1'b0), // 1-bit input: Controls double bit error injection on input data when
                                        // ECC enabled (Error injection capability is not available in
                                        // "decode_only" mode).
  
       .injectdbiterrb(1'b0), // 1-bit input: Controls double bit error injection on input data when
                                        // ECC enabled (Error injection capability is not available in
                                        // "decode_only" mode).
  
       .injectsbiterra(1'b0), // 1-bit input: Controls single bit error injection on input data when
                                        // ECC enabled (Error injection capability is not available in
                                        // "decode_only" mode).
  
       .injectsbiterrb(1'b0), // 1-bit input: Controls single bit error injection on input data when
                                        // ECC enabled (Error injection capability is not available in
                                        // "decode_only" mode).
  
       .regcea(1'b1),                 // 1-bit input: Clock Enable for the last register stage on the output
                                        // data path.
  
       .regceb(1'b1),                 // 1-bit input: Clock Enable for the last register stage on the output
                                        // data path.
  
       .rsta(reset),                     // 1-bit input: Reset signal for the final port A output register stage.
                                        // Synchronously resets output port douta to the value specified by
                                        // parameter READ_RESET_VALUE_A.
  
       .rstb(reset),                     // 1-bit input: Reset signal for the final port B output register stage.
                                        // Synchronously resets output port doutb to the value specified by
                                        // parameter READ_RESET_VALUE_B.
  
       .sleep(1'b0),                   // 1-bit input: sleep signal to enable the dynamic power saving feature.
       .wea(portA_we),                       // WRITE_DATA_WIDTH_A/BYTE_WRITE_WIDTH_A-bit input: Write enable vector
                                        // for port A input data port dina. 1 bit wide when word-wide writes are
                                        // used. In byte-wide write configurations, each bit controls the
                                        // writing one byte of dina to address addra. For example, to
                                        // synchronously write only bits [15-8] of dina when WRITE_DATA_WIDTH_A
                                        // is 32, wea would be 4'b0010.
  
       .web(portB_we)                        // WRITE_DATA_WIDTH_B/BYTE_WRITE_WIDTH_B-bit input: Write enable vector
                                        // for port B input data port dinb. 1 bit wide when word-wide writes are
                                        // used. In byte-wide write configurations, each bit controls the
                                        // writing one byte of dinb to address addrb. For example, to
                                        // synchronously write only bits [15-8] of dinb when WRITE_DATA_WIDTH_B
                                        // is 32, web would be 4'b0010.
  
    );

endmodule
