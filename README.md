# SGSafety

## Table of contents

* [General info](#general-info)
* [Technologies](#technologies)
* [Setup](#setup)
* [Aim Of Project](#aim-of-project)
* [Main page](#main-page)
* [Identify potential hazards](#identify-potential-hazards)
* [Sending panic request to close contacts](#sending-panic-request-to-close-contacts)
* [Conclusion](#conclusion)

## General info

This README contains the a brief overview of the SGSafety app which is a collaborative effort of a 6 member team:

## Technologies

The project is created using:

* Android Studio (Version: Arctic Fox | 2020.3.1)

## Aim Of Project

This application has 2 main uses:

1. The first main usage is to help users **identify potential hazards** in the area that they are in close proximity of

2. The second main usage is to allow users to **send panic request to close contacts** to allow users to send their location to their close contacts to see the user's current location in case the user is in need of assistance

## **Main page**

---

A snapshot of the main page of the application will look something as follows:

<p align="center">
    <img width="221" height="400" src="https://www.dropbox.com/s/tirsjokj2jtvvgu/main_page.png?raw=1">
<p>

## **Identify potential hazards**

---

The **identify potential hazards** feature in the app is to assist users to identify hazards in areas in Singapore that they are in close proximity of using measurements from Singapore's government database

The hazards the app identifies and their corresponding information are as shown:

1.
    ---

    * **Lightning alerts**

    * The data for lightning alerts are obtained using real time checking from [here](https://data.gov.sg/dataset/weather-forecast)

    * The data is updated every half-hourly to predict the forecast for the next 2 hours across multiple areas in Singapore

    * The lightning alert page will look something as follows:

    <p align="center">
        <img width="221" height="400" src="https://www.dropbox.com/s/ckqt0evisourp29/lightning_alert.png?raw=1">
    <p>

2.
    ---

    * **Dengue clusters**

    * The data for dengue clusters are obtained using real time checking from [here](https://data.gov.sg/dataset/dengue-clusters)

    * The data is updated daily to count the number of active dengue cases accumulated in each area across multiple areas in Singapore

    * The dengue cluster alert page will look something as follows:

    <p align="center">
        <img width="221" height="400" src="https://www.dropbox.com/s/hqxz642pnspsb24/dengue_cluster.png?raw=1">
    <p>

3.
    ---

    * **High temperatures**

    * The data for high temperatures are obtained using real time checking from [here](https://data.gov.sg/dataset/realtime-weather-readings)

    * The data is updated every minute to measure the air temperature readings across multiple areas in Singapore

    * The high temperature alert page will look something as follows:

    <p align="center">
        <img width="221" height="400" src="https://www.dropbox.com/s/chkvmbz0yyeo9ht/high_temperature.png?raw=1">
    <p>

4.
    ---

    * **Ultraviolet radiation**

    * The data for ultraviolet radiation are obtained using real time checking from [here](https://data.gov.sg/dataset/ultraviolet-index-uvi)

    * The data is updated every hour between 7am and 7pm daily to measure the ultraviolet raidation across the whole Singapore

    * The ultraviolet radiation alert page will look something as follows:

    <p align="center">
        <img width="221" height="400" src="https://www.dropbox.com/s/chkvmbz0yyeo9ht/high_temperature.png?raw=1">
    <p>

## **Sending panic request to close contacts**

---

The **Sending panic request to close contacts** feature in the app is to allow users to send their location to the users' close contacts to see the user's current location in case the user is in need of assistance

1. By default, the user will be shown a main page:

    <p align="center">
        <img width="221" height="400" src="https://www.dropbox.com/s/tirsjokj2jtvvgu/main_page.png?raw=1">
    <p>

2. For the users to add close contacts, he or she can swipe to the right tab to add contacts he/she wants to be notified in case the user themselves are in an emergency:

    <p align="center">
        <img width="221" height="400" src="https://www.dropbox.com/s/7t7v02s3rpunuuk/close_contact.png?raw=1">
    <p>

3. When a user decides that he/she is in danger and requires assistance, he/she can click the "Issue Panic Request" button

4. This will cause the homepage of the user in distressed to look as follows where the location sharing icon will be bolded

    <p align="center">
        <img width="221" height="400" src="https://www.dropbox.com/s/up6fqkq93f7dm5l/main_page_distress.png?raw=1">
    <p>

5. From the close contacts point of view, if say User456 is in distress, this is what will be shown on the close contacts phone:

    <p align="center">
        <img width="221" height="400" src="https://www.dropbox.com/s/o7yxzrwxfg98sml/receive_distress.png?raw=1">
    <p>

6. If the close contacts would like to find out more about User456, the close contact can click on the link of User456 and will be directed to a map interface of where User456 is currently located at as shown:

    <p align="center">
        <img width="221" height="400" src="https://www.dropbox.com/s/zyr3zsgr5dcejd4/map_unclicked.png?raw=1">
    <p>

7. Further clicking on the pin of location, the close contact will be given options to either be given directions to User456 or to view User456's location in Google Maps as shown:

    <p align="center">
        <img width="221" height="400" src="https://www.dropbox.com/s/e6x44zivzxfvttz/map_clicked.png?raw=1">
    <p>

## Conclusion

This application is made with the intention to help users to be more aware of hazards in Singapore. It also serves as a platform for users to send distress signals in the form of their location to close contacts.

Even though it may be a simple project, but our team hoped that this application can be of some help for people like you and me.

If there are any feedback or remarks about our application, please feel free to contact anyone of us, thank you.