package xml2txt;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

import common.LogUtils;

/**
 * Converterをスレッドに包むためのラッパ
 * ConverterDriverが監視するのに必要な機能も有する
 * @author 
 *
 */
public class ConverterThread extends Thread {
    Converter converter;
    ConcurrentLinkedQueue<ConvertJob> sQueue;
    boolean abendFlg = false;


    public ConverterThread(Converter converter,  ConcurrentLinkedQueue<ConvertJob> pQueue) {
        super();
        this.converter = converter;
        this.sQueue = pQueue;
    }

    /**
     * このスレッドがアベンドしたことを示すフラグ
     * @return
     */
    public boolean isAbend() {
        return this.abendFlg;
    }

    @Override
    public synchronized void run() {
        LogUtils.logMain.info("ワーカースレッド起動");
        try{
            while(true){
                
                ConvertJob job = this.sQueue.poll();
                if ( job == null){
                    //これ以上作業が無い
                    break;
                }
                try{
                    LogUtils.logMain.debug(job.makeJobString());
                    this.converter.convert(job.getInFile(),  job.getInFileEncode(), job.getOutFile(), job.getOutFileEncode(), job.isOutputWithBomFlg());
                    job.setStatus(ConvertJob.STATUS_SUCCESS); //処理成功
                }catch (Exception e) {
                    this.abendFlg = true;
                    job.setStatus(ConvertJob.STATUS_FAIL); //処理失敗
                    job.setException(e);
                    LogUtils.convertErr.error("変換に失敗しました。:" + job.makeJobLogStr(), e);
                }finally{
                    LogUtils.jobLogWrite(job);
                }
            } 
        }catch (Exception e) {
            this.abendFlg = true;
            LogUtils.logMain.error("ワーカースレッドがアベンドしました。", e);
        }
        LogUtils.logMain.info("ワーカースレッド終了");
    }
    public Converter getConverter(){
        return this.converter;
    }
    
    

}

