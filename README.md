# BUX Trader bot

## Introduction
A trader bot that would make you filthy rich.

## Requirements
mvn, docker and a linux based terminal.

## Consideration
For the sake of simplicity, the bot uses in memory persistence to track the trades made.

## How to run
run the script runme.sh with path of file containing products to trade in this format:

`./runme.sh INPUT`

PRODUCT_ID **-** BUY_PRICE **-** UPPER_SELL_PRICE **-** LOWER_SELL_PRICE

Prices should be **.** separated.
Example of a line in the file:

p1-12.3-98.2-1.2

More lines, more products, more $$$$$

I added a file named _sample_input_ for reference.

Make it rain folks