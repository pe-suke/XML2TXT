package xml2txt;

public class Data {
    private String path;
    private String value;
    public Data(String path, String value) {
        this.path = path;
        this.value = value;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

}
