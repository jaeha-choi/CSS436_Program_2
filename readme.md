# Program 2
### Author: Jaeha Choi

## Description
MyCity is a program that takes in a city name and prints weather information along with "interesting" information. I've decided to get elevation data of a provided location for interesting information.

## Design

### API
I am currently calling three different APIs from two different sources. The first API call is responsible for querying the city name to get coordinates of the city. This ensures the accuracy of the following API calls, as the first API can fetch all matching locations. The next API call is the weather API, which is used to show current temperature and weather. The last API call is made to fetch elevation data at the provided coordinate.

All API calls retry up to 3 times with increasing delay if the response status code shows server error (5xx).

If the first API call (location API) fails, the whole program stops. This is because the rest of the program depends on it. However, all other API calls will not prevent the program from executing even if they fail.

If there is more than one matching city with a given query, this program requests information for up to 5 different cities. If the query returns more than one city, there will be a 1-second delay after each city, to respect the API call limitations.

### Parameters
Users are expected to escape the input parameter, including the city name; if the city name contains a space, it must be passed with quotation marks on both ends. This program would not work if the user fails to put a city name with space without quotation marks. This design choice is intentional, and it's to prevent ambiguity and preserve consistency with other programs.

## Usage
```
usage: java -jar MyCity.jar [options] <city_name> <api_key>
 -c,--country-code <code>   country code (see ISO 3166)
 -h,--help                  print this message
 -s,--state-code <code>     state code (Only available for the US cities)
```

### Examples
A very simple shell script (`./src/build.sh`) is provided to meet the requirements of this assignment.

Weather and elevation data for `Seattle`
> `java -jar MyCity.jar Seattle api_key`

Weather and elevation data for `Seattle` in `US`
> `java -jar MyCity.jar -c=US Seattle api_key`

Weather and elevation data for `Seattle` in `WA`
> `java -jar MyCity.jar -s WA "Seattle" api_key`

Weather and elevation data for `New York` in `NY`, `US` 
> `java -jar MyCity.jar --country-code=US --state-code=NY "New York" api_key`

### Example Output
```
> java -jar MyCity.jar Seattle api_key
City name:      Seattle
State name:     Washington
Country code:   US
Temperature:    4.56°C / 40.21°F
Weather:        mist
Elevation:      52m / 170.60ft

City name:      Seattle
State name:     Jalisco
Country code:   MX
Temperature:    18.89°C / 66.00°F
Weather:        clear sky
Elevation:      1543m / 5062.34ft
```

### Misc

Unfortunately, location API of OpenWeatherMap seems to be return duplicate/incorrect results sometimes. 
For example, [querying "Lake City" in "WA" (appid required)](https://api.openweathermap.org/geo/1.0/direct?q=Lake%20City,WA,US&limit=5&appid=) returns Lake City in Iowa and Pennsylvania instead of only returning Lake City in Washington.
