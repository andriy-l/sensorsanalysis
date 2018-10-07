MQTT topics:
https://www.hivemq.com/blog/mqtt-essentials-part-5-mqtt-topics-best-practices

Best practices to limit application payloads:
https://www.thethingsnetwork.org/forum/t/best-practices-to-limit-application-payloads/1302

Choose proper payload size
https://iot.stackexchange.com/questions/3293/mqtt-multiple-topics-vs-bigger-payload




create database mqttdemo;
use mqttdemo;
create table sensors_data(id serial, publisher varchar(50), time date, temp double, humidity int);
grant all privileges on sensor_data.* to 'andriy'@'%';