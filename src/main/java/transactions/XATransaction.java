import com.atomikos.icatch.jta.UserTransactionManager;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.transaction.HazelcastXAResource;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionalMap;

import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import java.io.File;
import java.io.FilenameFilter;

public class XATransaction {

    public static void main(String[] args) throws Exception {
        //tag::xa[]
        cleanAtomikosLogs();

        HazelcastInstance instance = Hazelcast.newHazelcastInstance();
        HazelcastXAResource xaResource = instance.getXAResource();

        UserTransactionManager tm = new UserTransactionManager();
        tm.begin();

        Transaction transaction = tm.getTransaction();
        transaction.enlistResource(xaResource);
        TransactionContext context = xaResource.getTransactionContext();
        TransactionalMap<Object, Object> map = context.getMap("map");
        map.put("key", "val");
        transaction.delistResource(xaResource, XAResource.TMSUCCESS);

        tm.commit();

        IMap<Object, Object> m = instance.getMap("map");
        Object val = m.get("key");
        System.out.println("value: " + val);

        cleanAtomikosLogs();
        Hazelcast.shutdownAll();
        //end::xa[]
    }

    private static void cleanAtomikosLogs() {
        try {
            File currentDir = new File(".");
            final File[] tmLogs = currentDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".epoch") || name.startsWith("tmlog");
                }
            });
            for (File tmLog : tmLogs) {
                tmLog.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}