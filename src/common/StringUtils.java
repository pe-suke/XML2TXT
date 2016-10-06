package common;

import java.io.File;

public class StringUtils {
    

    /**
     * 相対パスを取得するメソッド
     * 期待ディレクトリから対象ファイルへの相対パスを求める
     * このメソッドは簡易版なのでtrgFileがtrgDirの配下にある場合しか使えない。
     * trgFileがtrgDirの外部にある場合はIllegalArgumentExceptionとなる
     * @param trgFile 相対パス取得対象のファイル
     * @param trgDir 規定となるディレクトリ
     * @return
     */
    public static String getRelativePathSimple(File trgFile, File trgDir){
        //このファイルのディレクトリ文字列を取得
        String trgFileDirPath = trgFile.getParentFile().getAbsolutePath();
        String trgDirPath = trgDir.getAbsolutePath();
        
        if ( trgFileDirPath.startsWith(trgDirPath)  ){ //必ずtrgDirが含まれているはず
            
            String ret = trgFileDirPath.substring(trgDirPath.length());
            if ( ret.length() >0){ //先頭のseparatorは取り除く
                if ( ret.startsWith(File.separator)){
                    ret = ret.substring(File.separator.length());
                }
            }            
            return ret;
        }else{
            throw new IllegalArgumentException(  "本メソッドは親ディレクトリの直下にあるファイルの相対パスしか求められません。 trgFile=" + trgFile.getAbsolutePath() + ",trgDir=" + trgDir.getAbsolutePath()  );
        }
    }
    
    
    /**
     * trgDirからのtrgFileへの相対構造を維持したまま、destDir配下に移動した際のフルパスファイル名を求める
     * @param trgFile
     * @param trgDir
     * @param destDir
     * @return
     */
    public static File getMovedFileName(File trgFile, File trgDir, File destDir){
        String relativePath = getRelativePathSimple(trgFile, trgDir);
        File outDir = destDir;
        if ( relativePath.length()>0){
            outDir = new File(destDir, relativePath);            
        }
        File outFile = new File(outDir, trgFile.getName());
        return outFile;        
    }
    
    /**
     * 拡張子を指定した文字列に変更する
     * @param srcFile 対象ファイル
     * @param ext 変更先拡張子
     * @return 変更したファイル名がセットされたFileを返却する
     */
    public static File changeExt(File srcFile, String ext){
        String filename = srcFile.getAbsolutePath();
        return new File(removeFileExtension(filename)+"." + ext);
        
    }
    private static String removeFileExtension(String filename) {
        int lastDotPos = filename.lastIndexOf('.');
        if (lastDotPos == -1) {
          return filename;
        } else if (lastDotPos == 0) {
          return filename;
        } else {
          return filename.substring(0, lastDotPos);
        }
      }
    
    public static boolean isEmpty(String s){
        if ( s == null){
            return true;
        }else{
            String s2 = s.trim();
            if ( s2.length() == 0){
                return true;
            }else{
                return false;
            }
        }
    }
    
}
