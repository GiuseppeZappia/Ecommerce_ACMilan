import 'package:flutter/material.dart';
import 'package:frontend/UI/ProfilePage.dart';
import 'package:frontend/UI/CartPage.dart';
import 'package:frontend/UI/PromotionsPage.dart';
import 'package:frontend/UI/ProductsPage.dart';
import 'package:frontend/UI/OrdersPage.dart';

class HomePage extends StatefulWidget {
  final int selectedIndex;
  HomePage({this.selectedIndex=0});
  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  late int _selectedIndex;

  static List<Widget> _widgetOptions = <Widget>[
    ProductsPage(),
    CartPage(),
    PromotionsPage(),
    OrdersPage(),
    ProfilePage(),
  ];

  @override
  void initState() {
    super.initState();
    _selectedIndex = widget.selectedIndex;
  }


  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Image.asset('assets/images/default.png',fit: BoxFit.contain,),
        ),
        title: Text('A.C. Milan Shop'),
        backgroundColor: Colors.red[800],
      ),
      body: Padding(
        padding: const EdgeInsets.all(8.0),
        child: _widgetOptions.elementAt(_selectedIndex),
      ),
      extendBody: true,
      bottomNavigationBar: Stack(
        children: [
          Positioned(
            bottom: 20,
            left: MediaQuery.of(context).size.width * 0.2,
            right: MediaQuery.of(context).size.width * 0.2,
            child: ClipRRect(
              borderRadius: BorderRadius.circular(30.0),
              child: Container(
                color: Colors.black,
                child: BottomNavigationBar(
                  items: const <BottomNavigationBarItem>[
                    BottomNavigationBarItem(
                      icon: Icon(Icons.home),
                      label: 'Prodotti',
                    ),
                    BottomNavigationBarItem(
                      icon: Icon(Icons.shopping_cart),
                      label: 'Carrello',
                    ),
                    BottomNavigationBarItem(
                      icon: Icon(Icons.local_offer),
                      label: 'Promozioni',
                    ),
                    BottomNavigationBarItem(
                      icon: Icon(Icons.list),
                      label: 'Ordini',
                    ),
                    BottomNavigationBarItem(
                      icon: Icon(Icons.person),
                      label: 'Profilo',
                    ),
                  ],
                  currentIndex: _selectedIndex,
                  selectedItemColor: Colors.red[800],
                  unselectedItemColor: Colors.grey,
                  onTap: _onItemTapped,
                  type: BottomNavigationBarType.fixed,
                  backgroundColor: Colors.transparent,
                  showUnselectedLabels: true,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
