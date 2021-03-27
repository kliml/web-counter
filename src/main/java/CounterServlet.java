import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Servlet is responsible for calculating the sum of numbers passed in the POST request
 * by validating the requests body and passing information further to the Dispatcher class.
 * Timeout time is parsed from the web.xml file.
 * @see Dispatcher
 */
@WebServlet(
    description = "Calculates the sum of numbers passed in the POST request",
    asyncSupported = true)
public class CounterServlet extends HttpServlet {

  final Pattern whiteSpacePattern = Pattern.compile("\\s+");

  AsyncListener asyncListener = new AsyncListener() {
    @Override
    public void onComplete(AsyncEvent event) throws IOException {
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
      Dispatcher.removeConnection(event.getAsyncContext());
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
    }
  };

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  public void destroy() {
    super.destroy();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String s = request.getReader().readLine();

    if (s == null) {
      response.sendError(400, "Empty request body");
      return;
    }

    String[] sd = whiteSpacePattern.split(s);

    if (!validateBody(sd)) {
      response.sendError(400, "Invalid request");
      return;
    }

    if (sd.length == 1) {

      long val = Long.parseLong(sd[0]);
      Dispatcher.addToCalculation(val);

      AsyncContext asyncContext = request.startAsync(request, response);
      asyncContext.addListener(asyncListener);
      asyncContext.setTimeout(120000);

      Dispatcher.addConnection(asyncContext);

    } else if (sd.length == 2) {

      AsyncContext asyncContext = request.startAsync(request, response);
      asyncContext.addListener(asyncListener);
      asyncContext.setTimeout(120000);

      Dispatcher.addConnection(asyncContext);
      Dispatcher.endCalculation(sd[1]);
    }
  }

  /**
   * Validates POST body
   * @param body POST body that was split on spaces.
   * @return condition of body, true if valid, otherwise false.
   */
  private boolean validateBody(String[] body) {
    if (body.length == 1) {
      try {
        Long.parseLong(body[0]);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    } else if (body.length == 2) {
      return  body[0].equals("end");
    } else {
      return false;
    }
  }
}
