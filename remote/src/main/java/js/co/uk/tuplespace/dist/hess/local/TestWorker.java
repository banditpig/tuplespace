package js.co.uk.tuplespace.dist.hess.local;

import js.co.uk.tuplespace.examples.SquareMaster;
import js.co.uk.tuplespace.space.Space;
import js.co.uk.tuplespace.task.Worker;

/**
 * 
 * @author mike
 */
public class TestWorker {

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        long max = 100L;

        js.co.uk.tuplespace.dist.hess.local.HessianLocalSpaceCreator localSpaceCreator = new js.co.uk.tuplespace.dist.hess.local.HessianLocalSpaceCreator("http://127.0.0.1:8080");

        Space space = localSpaceCreator.createSpace("squares");
        space.purgeAllEntries();
        space.getName();
        SquareMaster master = new SquareMaster(space, max);
        master.setMaxAllowedLevel(10);
        master.startGenerationAndCollection();

        for (int i = 0; i < 5; i++) {

            Worker worker = new Worker(space);
            worker.setSimulateFailures(true);
            worker.startWork();
        }

        System.out.println(space.size());

        // System.out.println("MAtch " + space.get(new SimpleTuple("*", "*", 3)));

        long res = max * (max + 1) * (2 * max + 1);
        System.out.println(res);



    }
}
