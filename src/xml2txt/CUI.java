package xml2txt;

import java.io.File;
import java.util.Date;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import common.LogUtils;
import common.StringUtils;

/**
 * コマンドラインから起動するときのパラメータ解析と
 * 結果を標準出力するためのモジュール
 * 
 * @author 
 *
 */
public class CUI {
    

    private static final String THREADS = "thread-count";
    private static final String TEMPLATE_FILE = "template-file";
    private static final String OUTPUT_DIR = "output-dir";
    private static final String INPUT_DIR = "input-dir";
    private static final String EXPORT_FLAT = "export-flat";
    private static final String SHOW_PERFORMANCE_LOG = "show-performance-log";
    private static final String OUTPUT_ENCODING = "output-encoding";
    private static final String INPUT_ENCODING = "input-encoding";
    private static final String OUTPUT_WITH_BOM = "output-with-bom";
    
    private boolean validateOk =true;
    private StringBuffer errorMsg = new StringBuffer();
    
    
    CommandLineParser parser;
    Options options ;
    CommandLine line;
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        CUI cui = new CUI();
        cui.execute(args);

    }
    
    public CUI(){
        parser = new BasicParser();
        options = new Options();
        options.addOption( OptionBuilder.withLongOpt(INPUT_DIR)
                .hasArg()
                .withDescription(  "変換元のXMLファイルのあるディレクトリ名を指定する。サブディレクトリも処理対象となる。(必須)" )                
                .create("i"));

        options.addOption( OptionBuilder.withLongOpt(OUTPUT_DIR)
                .hasArg()
                .withDescription(  "変換結果のTXTファイルを出力したいディレクトリ名を指定する(必須)" )
                .create("o"));
        
        options.addOption( OptionBuilder.withLongOpt(TEMPLATE_FILE)
                .hasArg()
                .withDescription(  "TXTの出力テンプレート(省略時=./xml2txt.vm)" )
                .create("t"));

        options.addOption( OptionBuilder.withLongOpt(THREADS)
                .hasArg()
                .withDescription(  "マルチスレッドオプション。変換を実施するワーカースレッド数を指定する(省略時=1)" )
                .create("c"));
        
        options.addOption( OptionBuilder.withLongOpt(OUTPUT_ENCODING)
                .hasArg()
                .withDescription(  "変換結果のTXTファイルを出力する際の文字コード(省略時=UTF-8)" )
                .create("oc"));
        
        options.addOption( OptionBuilder.withLongOpt(INPUT_ENCODING)
                .hasArg()
                .withDescription(  "変換元XMLの文字コードを指定する(省略時=UTF-8)" )
                .create("ic"));
        
        options.addOption( new Option("f", EXPORT_FLAT, false, "inputDir内のディレクトリ構造をoutputDir内に再現しない。これを指定した場合はinputDir内のサブディレクトリにあったファイルもすべてoutputDir直下に出力されるので、同名があると上書きされるので注意。(オプション)"));
        options.addOption( new Option("p", SHOW_PERFORMANCE_LOG, false ,"性能ログをコンソールに出力する。これを指定すると処理中１秒ごとに進捗とHeapの状態をコンソールやログに出力する。(オプション)"));
        options.addOption( new Option("b", OUTPUT_WITH_BOM, false, "出力TXTにUTF-8のBOMを出力する。これを指定するとUTF-8のBOMデータ(EF BB BF)を全ての出力TXTファイルに付与する。UTF-8用であるが指定すると" +OUTPUT_ENCODING+"がUTF-8以外であっても発動する。(オプション)"));

        
    }
    
    
    public void execute(String[] args) throws Exception {
        
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        
        if ( args.length == 0){        
            // ヘルプの表示
            formatter.printHelp( CUI.class.getName(), options );
            System.err.println("コマンドライン引数がありません");
            return ;
        }
        
        try{
            line = parser.parse( options, args );
        }catch(Exception e){
            formatter.printHelp( CUI.class.getName(), options );
            System.err.println("コマンドライン引数が不正です。:" + e.getMessage());
            return;
        }

        String iputDirName = getRequiredParam(INPUT_DIR, "変換元のディレクトリ名は必要です。");
        String outputDirName = getRequiredParam(OUTPUT_DIR, "変換先出力ディレクトリ名は必要です。");
        String templateFileName = getNotRequiredParam(TEMPLATE_FILE, "xml2txt.vm");
        String outputEncode = getNotRequiredParam(OUTPUT_ENCODING,"UTF-8");
        String inputEncode = getNotRequiredParam(INPUT_ENCODING,"UTF-8");
        
       
        int threadNumber = Integer.parseInt( getNotRequiredParam(THREADS, "1"));        
        boolean exportToFlatFlg = line.hasOption(EXPORT_FLAT);
        boolean showPerformanceLog = line.hasOption(SHOW_PERFORMANCE_LOG);
        boolean outputWithBomFlg = line.hasOption(OUTPUT_WITH_BOM);
        
        if (!this.validateOk){
            //入力チェックエラー
            formatter.printHelp( CUI.class.getName(), options );

            System.err.println("\n\r" + this.errorMsg.toString());
            return;
        }
        if ( showPerformanceLog){
            LogUtils.timeLog.info("性能ログ計測モードで起動しました。");
        }
        
        Date dtStart = new Date();
        
        ConverterDriver driver = new ConverterDriver( threadNumber, templateFileName);
        driver.setShowPerformanceLog(showPerformanceLog);
        ConvertJob[] resultJobs = driver.execute(new File(iputDirName), inputEncode, new File(outputDirName),outputEncode, outputWithBomFlg, exportToFlatFlg );
        
        //結果の集計
        ConvertJobCounter counter = new ConvertJobCounter(resultJobs);
        Date dtEnd = new Date();
        
        LogUtils.logMain.info("ファイル数=" + counter.getAll() + "成功=" + counter.getSuccess() + ",失敗=" + counter.getFail() + ",未処理=" + counter.getNone() + ",処理時間=" + ((dtEnd.getTime()-dtStart.getTime())/1000.)   );
        if ( counter.getAll() == counter.getSuccess() ){
            //全部成功の場合
            System.out.println("0");
        }else{
            //それ以外の場合は何か異常がある
            System.out.println("2");
        }
        
        
        
      
    }
    /**
     * 必須パラメータ取得＆エラーメッセージ
     * @param paramName
     * @param errorMsg
     * @return
     */
    private String getRequiredParam(String paramName, String errorMsg){
        String v = line.getOptionValue(paramName);
        if ( StringUtils.isEmpty(v)){
            //エラー
            this.validateOk=false;
            this.errorMsg.append( paramName + "-" + errorMsg + "\n\r");
        }
        return v;
        
    }
    /**
     * 必須ではないパラメータの取得（デフォルト値採用）
     * @param paramName
     * @param defaultValue
     * @return
     */
    private String getNotRequiredParam(String paramName, String defaultValue){
        String v = line.getOptionValue(paramName);
        if ( StringUtils.isEmpty(v)){
            //エラー
            v = defaultValue;
        }
        return v;        
    }
    


}
