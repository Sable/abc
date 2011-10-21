package joinpointinterfaces;

import classes.*;

jpi void Buying(Item i, int amt, double price);
jpi void BuyingBestSeller(Item i, int amt, double price, ShoppingSession ss) extends Buying(i,amt,price);
jpi void BuyingEcoFriendly(Item i, int amt, double price, ShoppingSession ss) extends Buying(i,amt,price);