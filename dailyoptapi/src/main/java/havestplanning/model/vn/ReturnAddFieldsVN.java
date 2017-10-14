package havestplanning.model.vn;

public class ReturnAddFieldsVN {
	private int size;
	private String description;
	private FieldListVN fields;
	
	
	public FieldListVN getFields() {
		return fields;
	}

	public void setFields(FieldListVN fields) {
		this.fields = fields;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public ReturnAddFieldsVN(int size) {
		super();
		this.size = size;
	}

	public ReturnAddFieldsVN() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ReturnAddFieldsVN(int size, String description) {
		super();
		this.size = size;
		this.description = description;
	}

	public ReturnAddFieldsVN(int size, String description, FieldListVN fields) {
		super();
		this.size = size;
		this.description = description;
		this.fields = fields;
	}
	
}
