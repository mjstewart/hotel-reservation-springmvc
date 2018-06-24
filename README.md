Hotel reservation system using spring boot and thymeleaf.

[![Youtube demo](https://github.com/mjstewart/hotel-reservation-springmvc/blob/master/hotel_thumb.png)](https://www.youtube.com/watch?v=A9QIAvK-aGc "Youtube demo")


This project implements a complex domain model powering a session based wizard form flow to book
a hotel room. The flow simulates a real world application in terms of having a variety
of booking options and business rules. 

Key features include

- Thymeleaf ajax fragments + javascript
- Google maps API integration
- Responsive CSS with semantic-ui
- Extensive spring mvc tests + mockito
- Hibernate
- Query DSL

This project provided the inspiration and use cases to create a thymeleaf extension for manipulating query strings.
https://github.com/mjstewart/thymeleaf-querystring contains a video tutorial using this project to demonstrate the
integration with spring data `PagingAndSortingRepository`.

# Install

This project has a dependency on https://github.com/mjstewart/thymeleaf-querystring. This dependency is not currently available on maven central. As per README in https://github.com/mjstewart/thymeleaf-querystring, you can simply download the jar in the root folder and manually add it to this project before running it.
