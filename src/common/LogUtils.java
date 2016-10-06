package common;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import xml2txt.ConvertJob;


public class LogUtils {

    public static Logger logMain = org.apache.log4j.Logger.getLogger("logMain");
    public static Logger jobLog= org.apache.log4j.Logger.getLogger("jobLog");
    public static Logger convertErr= org.apache.log4j.Logger.getLogger("convertErr");
    public static Logger timeLog= org.apache.log4j.Logger.getLogger("timeLog");
    

    
    public static void jobLogWrite(ConvertJob job){
        switch (job.getStatus()) {
        case ConvertJob.STATUS_NONE:
            jobLog.warn("[未処理]"+ job.makeJobLogStr()  );
            break;
        case ConvertJob.STATUS_SUCCESS:
            jobLog.info("[SUCCESS]"+ job.makeJobLogStr()  );
            break;
        case ConvertJob.STATUS_FAIL:
            jobLog.error("[FAIL]"+ job.makeJobLogStr() );            
            break;
        default:
            break;
        }
    }

}
