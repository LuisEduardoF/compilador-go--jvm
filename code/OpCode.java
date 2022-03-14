package code;

/*
 * Enumeração das instruções aceitas pela TM (Tiny Machine).
 * Adaptado do arquivo 'instruction.h'. 
 */
public enum OpCode {
	// ---------------------------------------------------
	// Basic ops
	
    NOOP("nop", 0),

    // ---------------------------------------------------
    // Arith ops
    
    iadd("iadd", 0),	// ADDi ix, iy, iz	; ix <- iy + iz
    fadd("fadd", 0),	// ADDf fx, fy, fz	; fx <- fy + fz
    isub("isub", 0),	// SUBi ix, iy, iz	; ix <- iy - iz
    fsub("fsub", 0),	// SUBf fx, fy, fz	; fx <- fy - fz
    imul("imul", 0),	// MULi ix, iy, iz	; ix <- iy * iz
    fmul("fmul", 0),	// MULf fx, fy, fz	; fx <- fy * fz
    idiv("idiv", 0),	// DIVi ix, iy, iz	; ix <- iy / iz
    fdiv("fdiv", 0),	// DIVf fx, fy, fz	; fx <- fy / fz
    irem("irem", 0),
    // Widen to float

    // ---------------------------------------------------
    // Logic ops
    
    // Logical OR
    OROR("OROR", 3), 	// OROR ix, iy, iz	; ix <- (bool) iy || (bool) iz
    // Equality
    EQUi("EQUi", 3), 	// EQUi ix, iy, iz	; ix <- iy == iz ? 1 : 0
    EQUf("EQUf", 3),	// EQUf ix, fy, fz	; ix <- fy == fz ? 1 : 0
    EQUs("EQUs", 3), 	// EQUs ix, iy, iz	; ix <- str_tab[iy] == str_tab[iz] ? 1 : 0
    // Less than
    LTHi("LTHi", 3), 	// LTHi ix, iy, iz	; ix <- iy < iz ? 1 : 0
    LTHf("LTHf", 3), 	// LTHi ix, fy, fz	; ix <- iy < iz ? 1 : 0
    LTHs("LTHs", 3), 	// LTHs ix, iy, iz	; ix <- str_tab[iy] < str_tab[iz] ? 1 : 0

    // ---------------------------------------------------
    // Branches and jumps
    
    // Absolute jump
    JUMP("JUMP", 1),	// JUMP addr		; PC <- addr
    // Branch on true
    BOTb("BOTb", 2), 	// BOTb ix, off		; PC <- PC + off, if ix == 1
    // Branch on false
    BOFb("BOFb", 2),	// BOFb ix, off		; PC <- PC + off, if ix == 0

    // ---------------------------------------------------
    // Loads and stores

    // Load word (from address)
    LDWi("LDWi", 2), 	// LDWi ix, addr	; ix <- data_mem[addr]
    LDWf("LDWf", 2), 	// LDWf fx, addr	; fx <- data_mem[addr]
    // Load immediate (constant)
    LDIi("LDIi", 2), 	// LDIi ix, int_const	; ix <- int_const
    LDIf("LDIf", 2),  	// LDIf fx, float_const	; fx <- float_const (must be inside an int)
    // Store word (to address)
    STWi("STWi", 2),  	// STWi addr, ix		; data_mem[addr] <- ix
    STWf("STWf", 2),  	// STWf addr, fx		; data_mem[addr] <- fx
           

    // ---------------------------------------------------
    // Strings
    
    // (These strings instructions are obviously not present in real archs.
    //  The rationale for creating and using them here is simply for convenience:
    //  Normally, these are handled by a low level language lib or OS system call
    //  such as those present in 'libc', etc. However, involving real OS interfaces
    //  here will complicate the game considerably. Thus, we cheat by creating
    //  this high level interface for string handling.)
    
    // Store string
    SSTR("SSTR", 1),  // SSTR str_const		; str_tab <- str_const
    // Catenate
    CATs("CATs", 3),  // CATs ix, iy, iz	; str_tab[ix] <- str_tab[iy] + str_tab[iz]
    // Bool to String
    B2Ss("B2Ss", 2),  // B2Ss ix, iy		; ix <- @str_tab <- register iy (as str)
    // Integer to String
    I2Ss("I2Ss", 2),  // I2Ss ix, iy		; ix <- @str_tab <- register iy (as str)
    // Real to String
    R2Ss("R2Ss", 2),  // R2Ss ix, fy		; ix <- @str_tab <- register fy (as str)

    // ---------------------------------------------------
    // System calls, for I/O (see below)
    
    CALL("CALL", 2); // CALL code, x
	
	// CALL (very basic simulation of OS system calls)
	// . code: sets the operation to be called.
	// . x: register involved in the operation.
	// List of calls:
	// ----------------------------------------------------------------------------
	// code | x  | Description
	// ----------- -----------------------------------------------------------
	// 0   | ix | Read int:   register ix <- int  from stdin
	// 1   | fx | Read real:  register fx <- real from stdin
	// 2   | ix | Read bool:  register ix <- bool from stdin (as int)
	// 3   | ix | Read str:   str_tab[ix] <- str from stdin
	// 4   | ix | Write int:  stdout <- register ix (as str)
	// 5   | fx | Write real: stdout <- register fx (as str)
	// 6   | ix | Write bool: stdout <- register ix (as str)
	// 7   | ix | Write str:  stdout <- str_tab[ix]
	// ----------------------------------------------------------------------------
	// OBS.: All strings in memory are null ('\0') terminated, like in C.
	// ----------------------------------------------------------------------------
	
	public final String name;
	public final int opCount;
	
	private OpCode(String name, int opCount) {
		this.name = name;
		this.opCount = opCount;
	}
	
	public String toString() {
		return this.name;
	}
	
}

