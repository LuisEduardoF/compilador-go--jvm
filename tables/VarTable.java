package tables;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import typing.Type;

public final class VarTable {

	// No mundo real isto certamente deveria ser um hash...
	// Implementação da classe não é exatamente Javanesca porque
	// tentei deixar o mais parecido possível com a original em C.
	private List<Entry> table = new ArrayList<Entry>();
	private int escopo;


	public int lookupVar(String s) {
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i).name.equals(s)) {
				return i;
			}
		}
		return -1;
	}
	
	public int addVar(String s, int line, Type type, int tamArray) {
		Entry entry = new Entry(s, line, type,tamArray);
		int idxAdded = table.size();
		table.add(entry);
		return idxAdded;
	}
	
	public void setEscopo(int i) {
		this.escopo = i;
	}
	
	public void addEntry(Entry e) {
		table.add(e);
		return;
	}
	
	public int getEscopo() {
		return this.escopo;
	}
	
	public Entry getEntry(int i) {
		return table.get(i);
	}
	
	public String getName(int i) {
		return table.get(i).name;
	}
	
	public int getSize() {
		return table.size();
	}
	
	public int getLine(int i) {
		return table.get(i).line;
	}
	
	public Type getType(int i) {
		return table.get(i).type;
	}

	public int getTamArray(int i) {
		return table.get(i).tamArray;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		f.format("Variables table:\n");
		for (int i = 0; i < table.size(); i++) {
			f.format("[vt] Entry %d -- name: %s, line: %d, type: %s, tamArray: %d\n", i,
	                 getName(i), getLine(i), getType(i).toString(),getTamArray(i));
		}
		f.close();
		return sb.toString();
	}
	
	private final class Entry {
		String name;
		int line;
		Type type;
		int tamArray;
		
		Entry(String name, int line, Type type, int tamArray) {
			this.name = name;
			this.line = line;
			this.type = type;
			if(type == Type.ARRAY_TYPE){
				this.tamArray = tamArray;
			}
			else{
				this.tamArray = 0;
			}
		}
	}
}
