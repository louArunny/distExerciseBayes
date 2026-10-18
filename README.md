# Bayes Spam Filter

Dieses Projet wurde von Louana (louana.berger@students.fhnw.ch) und Baran (rojdabaran.karakuyu@students.fhnw.ch) abgegeben.

## Output
Wenn man das Programm ausführt, kommt folgender output:
<img width="869" height="306" alt="image" src="https://github.com/user-attachments/assets/9f515f9d-6a63-4671-ac1e-08345aca2fa6" />

Die gegebenen ZIP Files befinden sich im Ressource-Root des Projekts.

### Beantwortung Frage
```
Falls ein Wort, etwa money in der Anlernphase nur in Ham-Mails vorkam, dann hat ei-
ne Mail, die das Wort money enthält, eine Spamwahrscheinlichkeit von 0 (Begr¨unden
Sie dies!)
```

_Ohne alpha würde P(money|S) = 0. Da wir für die Berechnung der Wahrscheinlichkeit die Were multiplizieren, wäre anschliessend q = P(money|H) / P(money|S) = 0.1 / 0 = ∞. 
Somit wäre auch die Mail niemals Spam:
1/(∞ + 1) = 0

Basierend auf dieser Formel (die Formel ist in der Implementierung umgekehrt und berechnet direkt die Wahrscheinlichkeit von Spam, nicht Ham):
<img width="343" height="82" alt="image" src="https://github.com/user-attachments/assets/d5e98acc-cd49-4039-895b-953b85b7d43c" />


