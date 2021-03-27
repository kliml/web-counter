import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

  private final Pattern whiteSpacePattern = Pattern.compile("\\s+");
  private long timeOutTime = 120000;
  private final Logger logger = Logger.getLogger(CounterServlet.class.getName());

  private final AsyncListener asyncListener = new AsyncListener() {
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
    timeOutTime = Long.parseLong(config.getInitParameter("timeOutTime"));
    logger.info("CounterServlet initialized");
  }

  @Override
  public void destroy() {
    super.destroy();
    logger.info("CounterServlet destroyed");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String requestBody = request.getReader().readLine();
    if (requestBody == null) {
      response.sendError(400, "Empty request body");
      logger.info("Bad request");
      return;
    }

    String[] requestBodyContent = whiteSpacePattern.split(requestBody);
    if (!validateBody(requestBodyContent)) {
      response.sendError(400, "Invalid request");
      return;
    }

    AsyncContext asyncContext = request.startAsync(request, response);
    asyncContext.addListener(asyncListener);
    asyncContext.setTimeout(timeOutTime);

    if (requestBodyContent.length == 1) {
      long val = Long.parseLong(requestBodyContent[0]);
      Dispatcher.addToCalculation(asyncContext, val);
    } else if (requestBodyContent.length == 2) {
      Dispatcher.finishCalculation(asyncContext, requestBodyContent[1]);
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
