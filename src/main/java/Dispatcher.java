import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Static class responsible for holding active connections and sum of all numbers.
 */
public class Dispatcher {

  private static final AtomicLong sum = new AtomicLong(0);
  private static final ConcurrentLinkedQueue<AsyncContext> contextsHolder = new ConcurrentLinkedQueue<AsyncContext>();

  /**
   * Adds requests value to the sum and store connection.
   * @param asyncContext information about connection.
   * @param value value from requests body.
   */
  public static synchronized void addToCalculation(AsyncContext asyncContext, long value) {
    contextsHolder.add(asyncContext);
    sum.addAndGet(value);
  }

  /**
   * Stores information about connection and calls for a response.
   * @param asyncContext information about connection.
   * @param tag tag to be passed in response.
   */
  public static synchronized void finishCalculation(AsyncContext asyncContext, String tag) {
    contextsHolder.add(asyncContext);
    sendRespond(tag);
  }

  /**
   * Removes AsyncContext from contextsHolder.
   * Call only occurs if connection is timed out.
   * @param asyncContext context to be removed.
   */
  public static synchronized void removeConnection(AsyncContext asyncContext) {
    contextsHolder.remove(asyncContext);
  }

  /**
   * Respond to all active connections with the accumulated number and a tag.
   * Reset stored sum.
   * @param tag a tag that follows number in the response.
   */
  private static synchronized void sendRespond(String tag) {
    long val = sum.getAndSet(0);

    while(!contextsHolder.isEmpty()) {
      AsyncContext asyncContext = contextsHolder.poll();
      ServletResponse response = asyncContext.getResponse();
      response.setContentType("text/plain");
      try {
        PrintWriter writer = asyncContext.getResponse().getWriter();
        writer.print(String.format("%d %s", val, tag));
        writer.flush();
        writer.close();
        asyncContext.complete();
      } catch (Exception e) {
        asyncContext.complete();
      }
    }
  }
}
