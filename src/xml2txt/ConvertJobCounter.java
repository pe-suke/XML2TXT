package xml2txt;

/**
 * ConverterJobを集計するモジュール
 * @author 
 *
 */
public class ConvertJobCounter {
    
    private int all =0;
    private int fail =0;
    private int success=0;
    private int none =0;

    /**
     * ConverterJobの配列を与えるとそれを集計する
     * @param jobs
     */
    public ConvertJobCounter(ConvertJob[] jobs) {
        for (int i = 0; i < jobs.length; i++) {
            
            ConvertJob curJob = jobs[i];
            switch (curJob.getStatus()) {
            case ConvertJob.STATUS_SUCCESS:
                success++;
                break;
            case ConvertJob.STATUS_FAIL:
                fail++;
                break;
            case ConvertJob.STATUS_NONE:
                none++;
                break;
            }
            all++;            
        }
        
    }

    public int getAll() {
        return all;
    }

    public int getFail() {
        return fail;
    }

    public int getSuccess() {
        return success;
    }

    public int getNone() {
        return none;
    }
    
    public int getWorked(){
        return fail + success;
    }
    
    
}
