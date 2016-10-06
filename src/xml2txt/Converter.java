package xml2txt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import common.LogUtils;
import common.StringUtils;

/**
 * 単一XMLファイルをTXTに変換する機能を持つ
 * これ自身はスレッド云々の機能を持っていない
 * @author TIS197051
 *
 */
public class Converter {


    VelocityEngine ve = new VelocityEngine();

    Template template;

    String converterName;

    public Converter(String converterName, String templateName) throws Exception {
        super();
        this.converterName = converterName;
//        this.ve.setProperty( RuntimeConstants.FILE_RESOURCE_LOADER_PATH,  ".");
        this.ve.setProperty( RuntimeConstants.FILE_RESOURCE_LOADER_PATH,  "");
        this.ve.init();
        template = ve.getTemplate(templateName);
        LogUtils.logMain.debug("テンプレート読み込み成功:" + templateName);
    }


    public void convert(File inFile, String inFileEncode,
                       File outFile, String outFileEncode ,  boolean outputWithBomFlg) throws Exception{

        Reader r = new InputStreamReader( new FileInputStream(inFile), inFileEncode); //強制的にUTF-8で読む
        InputSource src = new InputSource(r);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src); //DOM化

        ArrayList<Data> datas = new ArrayList<Data>();

        traverse(datas, doc.getDocumentElement(), "");

        //VelocityContextを作成＆整備
        VelocityContext context = new VelocityContext();
        context.put ("records", datas);


        //Velocityによる変換の実施
        if ( !outFile.getParentFile().exists()){
            outFile.getParentFile().mkdirs();
        }

        FileOutputStream fout = new FileOutputStream(outFile);

        if ( outputWithBomFlg){ //UTF-8のBOM出力
            fout.write( 0xef );
            fout.write( 0xbb );
            fout.write( 0xbf );
        }

        Writer out = new OutputStreamWriter(fout, outFileEncode);
        template.merge(context, out);
        out.flush();
        out.close();

    }

    /**
     * Domツリーを走査しながらそこにあるテキストデータをレコードとして取り出す。
     * Elementにはツリーを構成する枝とデータがある葉の２種類があり、枝の部分は
     * レコードとして取り出す必要が無いが、枝なのか葉なのかを決める明確なルールがないので
     * 以下のように決めている
     * ・Elementに子要素が無い場
     *   テキストノードが空文字列→あっても空のテキストが設定されたレコードとして取り出す
     *   テキストノードが空文字列ではない→レコードとして取り出す
     * ・Elementに子要素がある場合
     *   テキストノードが空文字列→レコードとして取り出さない
     *   テキストノードが空文字列ではない→レコードとして取り出す
     *
     * @param datas レコードとして認定したテキストデータを格納する先
     * @param node 現在処理中のNode
     * @param prefix 再帰処理してゆく際に積まれるnode階層文字列（フルパス求めるのに使用）
     */
    public void traverse(ArrayList<Data>datas, Node node, String prefix){

        String nodeName = node.getNodeName();

        String nodeFullName = prefix + "/" + nodeName;


        switch (node.getNodeType()) {

        case 1:
            //Elementの場合は子要素の有無、テキストノードの内容を判別しながらレコードを取り出す
            NodeList nl = node.getChildNodes();
            StringBuffer sbData = new StringBuffer();
            boolean hasChildElement = false;
            //テキストノードの抽出
            for (int i = 0; i < nl.getLength() ; i++) {
                Node curNode = nl.item(i);
                switch (curNode.getNodeType()) {
                case 3:
                case 4:
                    //テキスト,CDATA発見
                    String curValue = curNode.getNodeValue();
                    if ( StringUtils.isEmpty(curValue)){
                        continue;
                    }
                    sbData.append(curValue);
                    break;
                case 1:
                    //子要素を発見
                    hasChildElement=true;
                    break;
                case 8:
                    //コメントは無視
                    break;
                default:
                    throw new IllegalArgumentException("NodeType="+ curNode.getNodeType()  +"はサポートしていません。:場所=" + nodeFullName + "/" + curNode.getNodeName());
                }
            }
            //レコードを取り出すかどうか判断＆取り出し
            String data = sbData.toString();
            if ( StringUtils.isEmpty(data)){
                //空なら子要素がないときだけ
                if ( !hasChildElement){
                    datas.add(new Data(nodeFullName, data));
                }
            }else{
                //空でなければ出す
                datas.add(new Data(nodeFullName, data));
            }
        case 3:
        case 4:
            //テキスト,CDATA
            //テキストの連結はElementで先読み形式で実施するのでここでは何もやらない
            break;
        case 8:
            //コメントは無視
            break;

        default:
            throw new IllegalArgumentException("NodeType="+ node.getNodeType()  +"はサポートしていません。:場所="+ nodeFullName);

        }


        //子要素の再帰評価
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength() ; i++) {
            Node curNode = nl.item(i);
            traverse(datas, curNode, nodeFullName);
        }

    }









}
