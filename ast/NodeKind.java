package ast;

// Enumeração dos tipos de nós de uma AST.
// Adaptado da versão original em C.
// Algumas pessoas podem preferir criar uma hierarquia de herança para os
// nós para deixar o código "mais OO". Particularmente eu não sou muito
// fã, acho que só complica mais as coisas. Imagine uma classe abstrata AST
// com mais de 20 classes herdando dela, uma classe para cada tipo de nó...
public enum NodeKind {
	ASSIGN_NODE {
		public String toString() {
            return ":=";
        }
	},
    EQ_NODE {
		public String toString() {
            return "==";
        }
	},
    BLOCK_NODE {
		public String toString() {
            return "block";
        }
	},
    BOOL_VAL_NODE {
		public String toString() {
            return "";
        }
	},
    IF_NODE {
		public String toString() {
            return "if";
        }
	},
    INT_VAL_NODE {
		public String toString() {
            return "";
        }
	},
    LT_NODE {
		public String toString() {
            return "<";
        }
	},
	RT_NODE {
		public String toString() {
            return ">";
        }
	},
	ELT_NODE {
		public String toString() {
            return "<=";
        }
	},
	ERT_NODE {
		public String toString() {
            return ">=";
        }
	},
    MINUS_NODE {
		public String toString() {
            return "-";
        }
	},
    OVER_NODE {
		public String toString() {
            return "/";
        }
	},
    PLUS_NODE {
		public String toString() {
            return "+";
        }
	},
    FUNC_NODE {
		public String toString() {
            return "";
        }
	},
    PROGRAM_NODE {
		public String toString() {
            return "program";
        }
	},
    READ_NODE {
		public String toString() {
            return "read";
        }
	},
    REAL_VAL_NODE {
		public String toString() {
            return "";
        }
	},
    REPEAT_NODE {
		public String toString() {
            return "repeat";
        }
	},
    STR_VAL_NODE {
		public String toString() {
            return "";
        }
	},
    TIMES_NODE {
		public String toString() {
            return "*";
        }
	},
    VAR_DECL_NODE {
		public String toString() {
            return "var_decl";
        }
	},
    VAR_LIST_NODE {
		public String toString() {
            return "var_list";
        }
	},
    VAR_USE_NODE {
		public String toString() {
            return "var_use";
        }
	},
    WRITE_NODE {
		public String toString() {
            return "write";
        }
	},
    ARRAY_NODE {
		public String toString() {
            return "arr_";
        }
	};
	
	public static boolean hasData(NodeKind kind) {
		switch(kind) {
	        case BOOL_VAL_NODE:
	        case INT_VAL_NODE:
	        case REAL_VAL_NODE:
	        case STR_VAL_NODE:
	        case VAR_DECL_NODE:
	        case VAR_USE_NODE:
	        case FUNC_NODE:
	        case ARRAY_NODE:
	            return true;
	        default:
	            return false;
		}
	}
}
