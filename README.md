# web-counter

Simple Java servlet to count numbers from HTTP POST requests. \
Tested on Tomcat with 1000 active connections. \
Deployed servlet address without modifications - `http://localhost:8080/web-counter/`

## Request types

- Single number in body - keeps the number, does not respond yet.
- Keyword "end" followed by ID - responds with the sum of all received numbers to all open requests followed by the same ID.

## Building

Build war file with gradle `gradle war` or by using scripts:
- `gradlew war` for Linux.
- `gradlew.bat war` for Windows.
  
Or use prebuilt war file.

## Testing

Source code comes with `test-connections.sh` to test running servlet.

## Configuration

Request timeout time as well as servlet URL can be configured in the web.xml file.
