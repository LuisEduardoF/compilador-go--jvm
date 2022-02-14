package typing;

// Enumeração dos tipos primitivos que podem existir em GoLang.
// E um tipo composto: array.

public enum Type {
	NULL_TYPE {
		public String toString() {
	    		return "null";
		}
	},
	BOOL_TYPE {
		public String toString() {
	    		return "bool";
		}
	},
	INT_TYPE {
		public String toString() {
			return "int";
		}
	},
	STRING_TYPE {
		public String toString() {
	    		return "string";
		}
	},
	FLOAT_TYPE {
		public String toString() {
	    		return "float";
		}
	},
	IMAGINARY_TYPE {
		public String toString() {
	    		return "imaginary";
		}
	},
	RUNE_TYPE {
		public String toString() {
	    		return "rune";
		}
	},
	ARRAY_TYPE {
		public String toString() {
	    		return "array";
		}
	},
	NO_TYPE{
		public String toString() {
	    		return "no_type";
		}
	}
}
