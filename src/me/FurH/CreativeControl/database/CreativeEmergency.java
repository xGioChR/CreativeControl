package me.FurH.CreativeControl.database;

import java.sql.SQLException;
import me.FurH.CreativeControl.CreativeControl;
import me.FurH.CreativeControl.util.CreativeCommunicator;

/**
 *
 * @author FurmigaHumana
 */
public class CreativeEmergency extends Thread {
    
    public void loadup() {
        setName("Creative Emergency Thread");
        setPriority(Thread.MAX_PRIORITY);
        start();
    }
    
    @Override
    public void run() {
        CreativeSQLDatabase db = CreativeControl.getDb();
        CreativeCommunicator com = CreativeControl.getCommunicator();
        
        while (!db.lock.get()) {
            try {
                db.connection.commit();
            } catch (SQLException ex) {
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to set AutoCommit and commit the database, {0}.", ex, ex.getMessage());
            }

            while (!db.queue.isEmpty()) {
                String query = db.queue.poll();
                if (query != null) {
                    db.executeQuery(query, true, true);
                }
            }

            try {
                db.connection.commit();
            } catch (SQLException ex) {
                com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        "[TAG] Failed to set AutoCommit, {0}.", ex, ex.getMessage());
            }

            com.log("[TAG]: Extra queue thread disabled");
            db.emergency = false;
            interrupt();
        }
        interrupt();
    }
}
