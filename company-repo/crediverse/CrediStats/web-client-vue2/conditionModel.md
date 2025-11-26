# Campaign eligiblity modeling:

## Model 
@startuml
class Transaction {
  +evaluate(): boolean
}

abstract class Condition {
  +evaluate(transaction: Transaction): boolean
}
abstract class TransactionCondition extends Condition{
  - includeOrExclude
  +evaluate(transaction: Transaction): boolean
}
class AndCondition {
  -conditions: Condition[]
  +evaluate(transaction: Transaction): boolean
}
class OrCondition {
  -conditions: Condition[]
  +evaluate(transaction: Transaction): boolean
}
class LocationProperties extends TransactionCondition{
  +evaluate(transaction: Transaction): boolean
}
class SellerProperties extends TransactionCondition {
  +evaluate(transaction: Transaction): boolean
}
class BuyerProperties extends TransactionCondition {
  +evaluate(transaction: Transaction): boolean
}
class ProductProperties extends TransactionCondition {
  +evaluate(transaction: Transaction): boolean
}

Transaction "1" -- "1..*" Condition
Condition <|-- AndCondition
Condition <|-- OrCondition
@enduml

## Example eligibility condition:
### Criteria:
A transaction is eligible if the Seller is in Pretoria, and the buyer is in Pretoria or Johannesburg, and the product is a bundle that is in class A or class B or an artimesale of more than R100. 

### More formally

(
    (
        seller.location = Pretoria  
        AND 
        ( 
            buyer.location EQUALS "Pretoria" 
            OR 
            buyer.location EQUALS "Johannesburg"
        ) 
    )  
    AND 
    (
        (
            product.type EQUALS "Airtime"
            AND 
            product.value IS_GREATER_THAN "100"
        )
        OR 
        (
            product.type EQUALS "Bundle"
            AND 
            product.class EQUALS "ClassA"
        )
        OR 
        (
            product.type EQUALS "Bundle"
            AND product.class EQUALS "ClassB"
        )
    )
)




### Json representation
```
campaingEligibility: {
  AndCondition: [
    SellerPropertyCondition: {  
      include: true,
      location: "Pretoria"
    },
    OrCondition: [
      buyerPropertyCondition: {
        include: true,
        location: "Pretoria"
      },
      buyerPropertyCondition:{
        include: true,
        location: "Johannesburg"
      }
    ],
    OrCondition: [
      AndCondition :[
        productPropertyCondition: {
          include: true,
          saleType: "Airtime"
        },
        productPropertyCondition: {
          include: true,
          valueMoreThan: "100"
        }
      ], 
      OrCondition: [
        AndCondition: [
          productPropertyCondition: {
            include: true,
            saleType: "bundle"
          },
          productPropertyCondition: {
            include: true,
            bundleClass: "Class A"
          }
        ],
        AndCondition: [
          productPropertyCondition: {
            include: true,
            saleType: "bundle"
          },
          productPropertyCondition: {
            include: true,
            bundleClass: "Class B",
          }
        ]
      ]
    ]
  ]
}

```




Transactions are eligible if
