import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Static class responsible for holding active connections and sum of all numbers.
 */
public class Dispatcher {

  private static final AtomicLong sum = new AtomicLong(0);
  private static final ConcurrentLinkedQueue<AsyncContext> contextsHolder = new ConcurrentLinkedQueue<AsyncContext>();

  public static void addConnection(AsyncContext asyncContext) {
    contextsHolder.add(asyncContext);
  }

  /**
   * Removes AsyncContext from contextsHolder.
   * Call only occurs if connection is timed out.
   * @param asyncContext context to be removed.
   */
  public static void removeConnection(AsyncContext asyncContext) {
    contextsHolder.remove(asyncContext);
  }

  /**
   * Add long value to the sum.
   * @param value long parsed from request body.
   */
  public static void addToCalculation(long value) {
    sum.addAndGet(value);
  }

  /**
   * End calculation by responding to all active connections with the accumulated number and a tag.
   * Reset stored sum.
   * @param key a tag that follows number in the response.
   */
  public static synchronized void endCalculation(String key) {

    long val = sum.getAndSet(0);

    while(!contextsHolder.isEmpty()) {
      AsyncContext asyncContext = contextsHolder.poll();
      ServletResponse response = asyncContext.getResponse();
      response.setContentType("text/plain");
      try {
        PrintWriter writer = asyncContext.getResponse().getWriter();
        writer.print(String.format("%d %s", val, key));
        writer.flush();
        writer.close();
        asyncContext.complete();
      } catch (Exception e) {
        asyncContext.complete();
      }
    }
  }
}
