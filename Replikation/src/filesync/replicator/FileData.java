package filesync.replicator;

public class FileData {

	private String name;
	private String size;
	private String ctime;
	
	public FileData(String name, String size, String ctime) {
		this.name = name;
		this.size = size;
		this.ctime = ctime;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the size
	 */
	public String getSize() {
		return size;
	}
	/**
	 * @param size the size to set
	 */
	public void setSize(String size) {
		this.size = size;
	}
	/**
	 * @return the ctime
	 */
	public String getCtime() {
		return ctime;
	}
	/**
	 * @param ctime the ctime to set
	 */
	public void setCtime(String ctime) {
		this.ctime = ctime;
	}

}
