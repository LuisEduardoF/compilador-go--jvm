package tables;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import typing.Type;

public final class FuncTable {

	// No mundo real isto certamente deveria ser um hash...
	// ImplementaÃ§Ã£o da classe nÃ£o Ã© exatamente Javanesca porque
	// tentei deixar o mais parecido possÃ­vel com a original em C.
	private List<Entry> table = new ArrayList<Entry>(); 
	
	public int lookupFunc(String s) {
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i).name.equals(s)) {
				return i;
			}
		}
		return -1;
	}
	
	public int addFunc(String s, int line, List<Type> param, List<Type> retorno, VarTable vt) {
		Entry entry = new Entry(s, line, param, retorno, vt);
		int idxAdded = table.size();
		table.add(entry);
		return idxAdded;
	}
	
	public String getName(int i) {
		return table.get(i).name;
	}
	
	public List<Entry> getTable() {
		return table;
	}
	
	public int getLine(int i) {
		return table.get(i).line;
	}
	
	public List<Type> getTypes(int i) {
		return table.get(i).param;
	}

	public int getParamSize(int i) {
		return table.get(i).param.size();
	}
	
	public List<Type> getReturns(int i) {
		return table.get(i).retorno;
	}

	public String getTypesString(int i) {
		String retorno = "";
		for(Type t: table.get(i).retorno) {
			retorno += t.toString() + " ";
		}
		return retorno;
		
	}
	
	public VarTable getVarTable(int i) {
		return table.get(i).vt;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		f.format("Function tables:\n");
		for (int i = 0; i < table.size(); i++) {
			f.format("[ft] Entry %d -- name: %s, line: %d, retorno: %s\n%s\n", i,
	                 getName(i), getLine(i), getTypesString(i), getVarTable(i).toString());
		}
		f.close();
		return sb.toString();
	}
	
	private final class Entry {
		String name;
		int line;
		List<Type> param = new ArrayList<Type>();
		List<Type> retorno = new ArrayList<Type>();
		VarTable vt = new VarTable();
		
		Entry(String name, int line, List<Type> param, List<Type> retorno, VarTable vt) {
			this.name = name;
			this.line = line;
			this.param = param;
			this.retorno = retorno;
			this.vt = vt;
		}
	
	}
}
