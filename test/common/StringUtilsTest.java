package common;

import java.io.File;

import junit.framework.TestCase;;

public class StringUtilsTest extends TestCase {

    public void testGetRelativePathSimple(){
        
        
        assertEquals("dir2",  StringUtils.getRelativePathSimple(new File("c:\\root\\dir1\\dir2\\file.txt"), new File("c:\\root\\dir1"))   );
        assertEquals("dir2",  StringUtils.getRelativePathSimple(new File("/root/dir1/dir2/file.txt"), new File("/root/dir1"))   );
        assertEquals("dir2\\dir3",  StringUtils.getRelativePathSimple(new File("/root/dir1/dir2/dir3/file.txt"), new File("/root/dir1"))   );
        assertEquals("",  StringUtils.getRelativePathSimple(new File("/root/dir1/file.txt"), new File("/root/dir1"))   );
        
        try {
            System.out.println( StringUtils.getRelativePathSimple(new File("c:\\root\\dir3\\dir2\\file.txt"), new File("c:\\root\\dir1")));
            fail("例外にならないのはおかしい");
        } catch (Exception e) {
             
        }
    }
    
    public void testGetMovedFileName(){

        assertEquals(new File( "/root/target/dir2/file.txt"),  StringUtils.getMovedFileName(new File("/root/dir1/dir2/file.txt"), new File("/root/dir1"), new File( "/root/target")));
        assertEquals(new File( "/root/target/file.txt"),  StringUtils.getMovedFileName(new File("/root/dir1/file.txt"), new File("/root/dir1"), new File( "/root/target")));

        
    }
    
    public void testChangeExt(){
        
        assertEquals(new File("c:\\proram files\\data\\pst.file\\test.txt"), StringUtils.changeExt(new File("c:\\proram files\\data\\pst.file\\test.xml"), "txt") );
        
    }
    
}
