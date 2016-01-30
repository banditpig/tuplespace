package dist;
import js.co.uk.tuplespace.comms.StringMessage;
import js.co.uk.tuplespace.dist.hess.local.HessianLocalSpaceCreator;
import js.co.uk.tuplespace.space.Space;
import org.junit.*;
import static org.junit.Assert.*;
/**
 * Created by IntelliJ IDEA.
 * User: mike
 * Date: 09/12/2011
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
public class MessageTest {

    private static Space space;

    /**
     * Sets the up before class.
     *
     * @throws Exception the exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        HessianLocalSpaceCreator localSpaceCreator  = new HessianLocalSpaceCreator("http://127.0.0.1:8080");

       
        space = localSpaceCreator.createSpace( "messages");

    }

    /**
     * Tear down after class.
     *
     * @throws Exception the exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void templateWorksOnRetrieve(){
        space.purgeAllEntries();
        StringMessage freddyTemplate = new StringMessage("freddy");
        StringMessage joeTemplate = new StringMessage("freddy");

        StringMessage mFreddy = new StringMessage("freddy", "hi fred");
        StringMessage mJoe = new StringMessage("joe", "hi joe");

        space.put(mFreddy);
        space.put(mJoe);

        StringMessage ret = (StringMessage)space.get(freddyTemplate);
        assertEquals("hi fred", ret.getMessage()) ;
        assertEquals("freddy", ret.getReceiver()) ;
        assertEquals(1, space.size());

        ret = (StringMessage)space.get(joeTemplate);
        assertEquals("hi joe", ret.getMessage()) ;
        assertEquals("joe", ret.getReceiver()) ;
        assertEquals(0, space.size());
    }

}
