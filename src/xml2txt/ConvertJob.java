package xml2txt;

import java.io.File;

/**
 * 変換ジョブ
 * １つのXMLから1つのTXTへの変換に関する入出力や結果を保持することが出来る
 * @author 
 *
 */
public class ConvertJob {
    
    /** 
     * 未処理
     */
    public static final int STATUS_NONE = 0;
    /**
     * 成功
     */
    public static final int STATUS_SUCCESS = 1;
    /**
     * 失敗
     */
    public static final int STATUS_FAIL = 2;
    
    private File inFile;
    private String inFileEncode;
    private File outFile;
    private String outFileEncode;
    private boolean outputWithBomFlg;
    private int status = STATUS_NONE;
    private Exception exception;
    
    
    
    public ConvertJob(File inFile, String inFileEncode, File outFile, String outFileEncode, boolean outputWithBomFlg) {
        super();
        this.inFile = inFile;
        this.inFileEncode = inFileEncode;
        this.outFile = outFile;
        this.outFileEncode = outFileEncode;
        this.outputWithBomFlg = outputWithBomFlg;
    }
    
    
    public String makeJobString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ConvertJob{");
        sb.append("inFile=");
        sb.append(this.inFile.getAbsolutePath());
        sb.append("(");
        sb.append(this.inFileEncode);
        sb.append("),outFile=");
        sb.append(this.outFile.getAbsolutePath());
        sb.append("(");
        sb.append(this.outFileEncode);
        if (outputWithBomFlg){
            sb.append(",BOM付き");
        }
        sb.append("),success=");
        sb.append(this.status);
        sb.append("}");
        return sb.toString();
    }
    public String makeJobLogStr() {
        StringBuffer sb = new StringBuffer();        
        sb.append("inFile=");
        sb.append(this.inFile.getAbsolutePath());
        sb.append("(");
        sb.append(this.inFileEncode);
        sb.append("),outFile=");
        sb.append(this.outFile.getAbsolutePath());
        sb.append("(");
        sb.append(this.outFileEncode);
        if (outputWithBomFlg){
            sb.append(",BOM付き");
        }
        sb.append(")");
        return sb.toString();
    }
    

    
    public File getInFile() {
        return inFile;
    }
    public void setInFile(File inFile) {
        this.inFile = inFile;
    }
    public String getInFileEncode() {
        return inFileEncode;
    }
    public void setInFileEncode(String inFileEncode) {
        this.inFileEncode = inFileEncode;
    }
    public File getOutFile() {
        return outFile;
    }
    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }
    public String getOutFileEncode() {
        return outFileEncode;
    }
    public void setOutFileEncode(String outFileEncode) {
        this.outFileEncode = outFileEncode;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public boolean isOutputWithBomFlg() {
        return outputWithBomFlg;
    }
    public void setOutputWithBomFlg(boolean outputWithBomFlg) {
        this.outputWithBomFlg = outputWithBomFlg;
    }
    public Exception getException() {
        return exception;
    }
    public void setException(Exception exception) {
        this.exception = exception;
    }


}
