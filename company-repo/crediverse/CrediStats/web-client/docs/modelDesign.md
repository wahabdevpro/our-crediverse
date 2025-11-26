@startuml

class Campaign
class Condition
class Incentive 
class Communication 
class Segment
class LocationList

Campaign *--> Condition
Campaign *--> "0-*" Incentive
Campaign *--> "0-*" Communication
Condition o--> Segment
Condition o--> LocationList 

Condition *--> "0-*" Condition: subConditions

@enduml
