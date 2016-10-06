package xml2txt;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import common.LogUtils;
import common.StringUtils;


/**
 * 複数のスレッドを操りConverterをマルチスレッドで処理するモジュール
 * @author 
 *
 */
public class ConverterDriver {
    
    int threads = 0;
    Converter[] converters;
    ConvertJob[] convertJobs;
    
    boolean showPerformanceLog = false;
    
    
    ConcurrentLinkedQueue<ConvertJob> jobQueue;

    
    //初期化
    public ConverterDriver(int threads, String template) throws Exception {
        super();
        this.threads = threads;
        
        LogUtils.logMain.info("パラメータ:スレッド数=" + threads);
        LogUtils.logMain.info("パラメータ:template=" + template);
        
        //Converterの初期化
        ArrayList<Converter> alu = new ArrayList<Converter>();        
        for (int i = 0; i < this.threads; i++) {
            alu.add(new Converter("Converter" +i, template));            
        }        
        this.converters = alu.toArray( new Converter[0] );
        
    }
    
    


    public void setShowPerformanceLog(boolean showPerformanceLog) {
        this.showPerformanceLog = showPerformanceLog;
    }


    //主処理
    public ConvertJob[] execute(File srcDir, String srcEncoding, 
                                File destDir, String destEncoding,  boolean outputWithBomFlg,
                                boolean exportToFlatFlg) throws FileNotFoundException, InterruptedException{
      
        LogUtils.logMain.info("パラメータ:inputDir=" + srcDir.getAbsolutePath());
        LogUtils.logMain.info("パラメータ:srcEncoding=" + srcEncoding);
        if (!srcDir.isDirectory()){
            throw new FileNotFoundException(srcDir.getAbsolutePath() + "が存在しないか、ディレクトリではありません。");
        }
        LogUtils.logMain.info("パラメータ:outputDir=" + destDir.getAbsolutePath());
        LogUtils.logMain.info("パラメータ:destEncoding=" + destEncoding);
        if (!destDir.isDirectory()){
            throw new FileNotFoundException(srcDir.getAbsolutePath() + "が存在しないか、ディレクトリではありません。");
        }
        LogUtils.logMain.info("パラメータ:exportToFlatFlg=" + exportToFlatFlg);
        LogUtils.logMain.info("パラメータ:outputWithBomFlg=" + outputWithBomFlg);
        
        //ファイル一覧の取得
        makeTargetQueue(srcDir, srcEncoding, destDir, destEncoding, outputWithBomFlg, "xml", exportToFlatFlg);
        LogUtils.logMain.info("処理件数=" + this.jobQueue.size());
        
        
        Date start = new Date();
        

        ConverterThread[] converterThreads = new ConverterThread[this.threads];

        //スレッドの開始
        for (int i = 0; i < this.threads; i++) {
            ConverterThread convertetThread = new ConverterThread(this.converters[i], jobQueue);
            converterThreads[i] = convertetThread;
            convertetThread.start();
        }
        
        
        //終了待ち
        MessageFormat mf = new MessageFormat("進捗：{0}/{1}, 経過秒数={2,number,#.#}, 残秒数予想={3,number,#.#}, 処理経過（成功={4}, 失敗={5}）, 平均処理速度={6,number,#}件/秒, Heap残={7}bytes(確保={8}bytes)");

        while(true){
            Thread.sleep(1000);//1秒ごとにチェック
            int aliveCount =0;
            for (int i = 0; i < converterThreads.length; i++) {
                ConverterThread curThread = converterThreads[i];
                //稼働フラグチェック
                if ( curThread.isAlive()){
                    aliveCount++;
                }
            }
            if (aliveCount == 0){
                //すべて終了なら抜ける
                break;
            }
            
            
            if ( this.showPerformanceLog){ //性能ログ出力モードになってれば実行する
                ConvertJobCounter counter = new ConvertJobCounter(this.convertJobs);
                Date now = new Date();
                double deltaSec = (now.getTime()- start.getTime())/1000.; //経過秒数
                
                long totalMem =  Runtime.getRuntime().totalMemory();
                long freeMem = Runtime.getRuntime().freeMemory();
    
                LogUtils.timeLog.info(mf.format(new Object[]{ counter.getWorked(), counter.getAll(), deltaSec, 
                                                              (counter.getWorked()==0) ? "-" : deltaSec / ( 1.0 * counter.getWorked() / counter.getAll() ) -deltaSec  , //残り秒数
                                                                  counter.getSuccess(),                                 //成功 
                                                                  counter.getFail(),               //失敗
                                                                  counter.getWorked()/deltaSec,
                                                                  freeMem,
                                                                  totalMem}) );
            }
        }
        
        return this.convertJobs;
        
        
    }
    
    
    /**
     * 指定したディレクトリ配下にあるXMLファイルを取り出してジョブ定義を作成する
     * @param srcDir 処理対象ディレクトリ
     * @param srcEncoding 処理対象ディレクトリにあるファイルのエンコーディング
     * @param destDir 出力先ディレクトリ
     * @param destEncoding 出力ファイルのエンコーディング
     * @param outputWithBomFlg 出力ファイルにUTF-8 BOMを出力するならTrue
     * @param targetExt XMLファイルの拡張子(ALL小文字でセットすること）
     * @param exportFlatFlg srcDir以下の階層を保持しないならtrue
     */
    private void makeTargetQueue( File srcDir, String srcEncoding, File destDir, String destEncoding, boolean outputWithBomFlg, String targetExt, boolean exportFlatFlg){
        this.jobQueue =  new ConcurrentLinkedQueue<ConvertJob>();
        makeTargetQueueSub(srcDir, srcDir, srcEncoding, destDir, destEncoding, outputWithBomFlg, targetExt, exportFlatFlg);
        
        this.convertJobs = this.jobQueue.toArray(new ConvertJob[0]); //キューの外にも保存しておく
        
    }
    private void makeTargetQueueSub( File curDir, File srcDir, String srcEncoding, File destDir, String destEncoding, boolean outputWithBomFlg, String targetExt, boolean exportFlatFlg){
        File[] files =  curDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];           
            if ( file.isDirectory()  ){
                //fileがディレクトリの場合
                makeTargetQueueSub(file, srcDir, srcEncoding,  destDir, destEncoding, outputWithBomFlg, targetExt, exportFlatFlg);
            }else{
                //拡張子がxmlであれば処理する
                if (file.getAbsolutePath().toLowerCase().endsWith("." + targetExt)){
                    //対象ファイル
                    ConvertJob job = makeConvertJob(file, srcDir, srcEncoding, destDir, destEncoding, outputWithBomFlg, exportFlatFlg);
                    this.jobQueue.add(job); //処理対象をキューに
                }
                
            }
        }
    }
    private ConvertJob makeConvertJob(File inFile, File srcDir, String srcEncoding, File destDir, String destEncoding, boolean outputWithBomFlg, boolean exportFlatFlg ){
        
        File outFile;
        if ( exportFlatFlg){
            //階層保持しない
            outFile = StringUtils.changeExt(new File(destDir, inFile.getName()),"txt");
        }else{
            //階層保持する
            outFile = StringUtils.changeExt(StringUtils.getMovedFileName(inFile, srcDir, destDir),"txt");
        }
                
        ConvertJob job = new ConvertJob(inFile, srcEncoding, outFile, destEncoding, outputWithBomFlg);
        
        LogUtils.logMain.debug(job.makeJobString());
        return job;
        
    }
    

    
    


}
