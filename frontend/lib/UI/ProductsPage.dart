import 'package:flutter/material.dart';
import 'package:frontend/model/Model.dart';
import 'package:frontend/model/objects/Prodotto.dart';
import 'package:frontend/UI/ProductDetailsPage.dart';
import 'package:frontend/model/managers/RestManager.dart';

import '../model/objects/Utente.dart';

class ProductsPage extends StatefulWidget {
  @override
  _ProductsPageState createState() => _ProductsPageState();
}

class _ProductsPageState extends State<ProductsPage> {
  List<Prodotto>? _products;
  bool _isLoading = true;
  RestManager _restManager = RestManager();
  TextEditingController _searchController = TextEditingController();
  TextEditingController _minPriceController = TextEditingController();
  TextEditingController _maxPriceController = TextEditingController();
  String _selectedCategory = 'Qualunque';
  String _selectedSortCriteria = 'prezzo';  // Default sort criteria
  Map<int, int> _quantities = {};  // Map to keep track of product quantities

  Map<String, String> categoryImageMap = {
    'Tazze': 'assets/images/tazza2.webp',
    'Portachiavi': 'assets/images/portachiavi.jpg',
  };

  List<String> categories = ['Qualunque', 'Tazze', 'Portachiavi', "Palloni", "Bracciali", "Cinture", "Sciarpe"];
  List<String> sortCriteria = ['nome', 'categoria', 'prezzo', 'quantita'];

  @override
  void initState() {
    super.initState();
    _fetchProducts();
  }

  Future<void> _fetchProducts() async {
    final products = _selectedCategory == 'Qualunque'
        ? await Model.sharedInstance.elencoProdotti(0, 20, _selectedSortCriteria)
        : await Model.sharedInstance.elencoProdottiPerCategoria(0, 20, _selectedSortCriteria, _selectedCategory);

    setState(() {
      _products = products;
      _isLoading = false;
    });
  }

  Future<void> _addToCart(int productId) async {
    try {
      Utente? loggedUser = await Model.sharedInstance.getLoggedUser();
      if (loggedUser == null){
        showDialog(
          context: context,
          builder: (context) => AlertDialog(
            title: Text("Autenticazione richiesta"),
            content: Text("Effettua il login o registrati per aggiungere al carrello."),
            actions: [
              TextButton(
                onPressed: () {
                  Navigator.of(context).pop();
                },
                child: Text("OK"),
              ),
            ],
          ),
        );
      } else {
        int quantity = _quantities[productId] ?? 1;
        await Model.sharedInstance.aggiungiAcarrello(loggedUser.id, productId, quantity);
      }
    } catch (e) {
      print(e);
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Errore nell\'aggiunta al carrello')));
    }
  }

  void _showProductDetails(Prodotto product) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => ProductDetailsPage(product: product),
      ),
    );
  }

  Future<void> _searchProducts(String query) async {
    setState(() {
      _isLoading = true;
    });

    final products = await Model.sharedInstance.trovaProdottoPerNome(query);
    setState(() {
      _products = products;
      _isLoading = false;
    });
  }

  Future<void> _searchProductsByPriceRange() async {
    double minPrice = _minPriceController.text.isEmpty ? 1 : double.parse(_minPriceController.text);
    double maxPrice = _maxPriceController.text.isEmpty ? double.maxFinite : double.parse(_maxPriceController.text);

    setState(() {
      _isLoading = true;
    });

    final products = await Model.sharedInstance.ricercaPerFasciaPrezzo(0, 20, minPrice, maxPrice);
    setState(() {
      _products = products;
      _isLoading = false;
    });
  }

  void _onCategoryChanged(String? newCategory) {
    setState(() {
      _selectedCategory = newCategory!;
      _isLoading = true;
    });
    _fetchProducts();
  }

  void _onSortCriteriaChanged(String? newCriteria) {
    setState(() {
      _selectedSortCriteria = newCriteria!;
      _isLoading = true;
    });
    _fetchProducts();
  }

  // @override
  // Widget build(BuildContext context) {
  //   return Scaffold(
  //     appBar: AppBar(
  //       title: Text('Prodotti'),
  //       bottom: PreferredSize(
  //         preferredSize: Size.fromHeight(96.0),
  //         child: Padding(
  //           padding: const EdgeInsets.symmetric(horizontal: 8.0),
  //           child: Column(
  //             children: [
  //               Row(
  //                 children: [
  //                   Expanded(
  //                     child: TextField(
  //                       controller: _searchController,
  //                       decoration: InputDecoration(
  //                         hintText: 'Cerca prodotti...',
  //                         suffixIcon: IconButton(
  //                           icon: Icon(Icons.search),
  //                           onPressed: () => _searchProducts(_searchController.text),
  //                         ),
  //                       ),
  //                       onSubmitted: _searchProducts,
  //                     ),
  //                   ),
  //                   SizedBox(width: 8.0),
  //                   DropdownButton<String>(
  //                     value: _selectedCategory,
  //                     items: categories.map((String category) {
  //                       return DropdownMenuItem<String>(
  //                         value: category,
  //                         child: Text(category),
  //                       );
  //                     }).toList(),
  //                     onChanged: _onCategoryChanged,
  //                   ),
  //                   SizedBox(width: 8.0),
  //                   DropdownButton<String>(
  //                     value: _selectedSortCriteria,
  //                     items: sortCriteria.map((String criteria) {
  //                       return DropdownMenuItem<String>(
  //                         value: criteria,
  //                         child: Text(criteria),
  //                       );
  //                     }).toList(),
  //                     onChanged: _onSortCriteriaChanged,
  //                   ),
  //                 ],
  //               ),
  //               Row(
  //                 children: [
  //                   Expanded(
  //                     child: TextField(
  //                       controller: _minPriceController,
  //                       decoration: InputDecoration(
  //                         hintText: 'Prezzo minimo',
  //                       ),
  //                       keyboardType: TextInputType.number,
  //                     ),
  //                   ),
  //                   SizedBox(width: 8.0),
  //                   Expanded(
  //                     child: TextField(
  //                       controller: _maxPriceController,
  //                       decoration: InputDecoration(
  //                         hintText: 'Prezzo massimo',
  //                       ),
  //                       keyboardType: TextInputType.number,
  //                     ),
  //                   ),
  //                   IconButton(
  //                     icon: Icon(Icons.search),
  //                     onPressed: _searchProductsByPriceRange,
  //                   ),
  //                 ],
  //               ),
  //             ],
  //           ),
  //         ),
  //       ),
  //     ),
  //     body: _isLoading
  //         ? Center(child: CircularProgressIndicator())
  //         : _products == null || _products!.isEmpty
  //         ? Center(child: Text('NESSUN PRODOTTO CON QUESTE CARATTERISTICHE'))
  //         : LayoutBuilder(
  //       builder: (context, constraints) {
  //         int crossAxisCount = 2;
  //         if (constraints.maxWidth > 750 && constraints.maxWidth <= 990) {
  //           crossAxisCount = 3;
  //         } else if (constraints.maxWidth > 990 && constraints.maxWidth < 1300) {
  //           crossAxisCount = 4;
  //         } else if (constraints.maxWidth >= 1300) {
  //           crossAxisCount = 5;
  //         }
  //
  //         return Padding(
  //           padding: const EdgeInsets.all(8.0),
  //           child: GridView.builder(
  //             gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
  //               crossAxisCount: crossAxisCount,
  //               childAspectRatio: 0.8,
  //               mainAxisSpacing: 16.0,
  //               crossAxisSpacing: 16.0,
  //             ),
  //             itemCount: _products?.length ?? 0,
  //             itemBuilder: (context, index) {
  //               final product = _products![index];
  //               final imagePath = categoryImageMap[product.categoria] ?? 'assets/images/default.jpg';
  //               int quantity = _quantities[product.id] ?? 1;
  //
  //               return GestureDetector(
  //                 onTap: () => _showProductDetails(product),
  //                 child: Card(
  //                   margin: EdgeInsets.all(4.0),
  //                   child: Column(
  //                     mainAxisSize: MainAxisSize.min,
  //                     crossAxisAlignment: CrossAxisAlignment.start,
  //                     children: <Widget>[
  //                       AspectRatio(
  //                         aspectRatio: 1.0,
  //                         child: Image.asset(
  //                           imagePath,
  //                           fit: BoxFit.cover,
  //                         ),
  //                       ),
  //                       Padding(
  //                         padding: const EdgeInsets.all(8.0),
  //                         child: Row(
  //                           mainAxisAlignment: MainAxisAlignment.spaceBetween,
  //                           children: [
  //                             Expanded(
  //                               child: Column(
  //                                 crossAxisAlignment: CrossAxisAlignment.start,
  //                                 children: [
  //                                   Text(
  //                                     product.nome,
  //                                     style: TextStyle(fontSize: 14),
  //                                     maxLines: 1,
  //                                     overflow: TextOverflow.ellipsis,
  //                                   ),
  //                                   Text(
  //                                     '€${product.prezzo.toStringAsFixed(2)}',
  //                                     style: TextStyle(fontSize: 12),
  //                                   ),
  //                                 ],
  //                               ),
  //                             ),
  //                             Row(
  //                               children: [
  //                                 IconButton(
  //                                   icon: Icon(Icons.remove,color:Colors.red),
  //                                   onPressed: () {
  //                                     setState(() {
  //                                       if (quantity > 1) {
  //                                         quantity--;
  //                                         _quantities[product.id] = quantity;
  //                                       }
  //                                     });
  //                                   },
  //                                 ),
  //                                 Text(quantity.toString(), style: TextStyle(fontSize: 16.0)),
  //                                 IconButton(
  //                                   icon: Icon(Icons.add,color:Colors.green),
  //                                   onPressed: () {
  //                                     setState(() {
  //                                       quantity++;
  //                                       _quantities[product.id] = quantity;
  //                                     });
  //                                   },
  //                                 ),
  //                               ],
  //                             ),
  //                             IconButton(
  //                               icon: Icon(Icons.add_shopping_cart,color: Colors.blue),
  //                               onPressed: () => _addToCart(product.id),
  //                             ),
  //                           ],
  //                         ),
  //                       ),
  //                     ],
  //                   ),
  //                 ),
  //               );
  //             },
  //           ),
  //         );
  //       },
  //     ),
  //   );
  // }
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Prodotti'),
        bottom: PreferredSize(
          preferredSize: Size.fromHeight(120.0),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 8.0),
            child: Column(
              children: [
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _searchController,
                        decoration: InputDecoration(
                          hintText: 'Cerca prodotti...',
                          suffixIcon: IconButton(
                            icon: Icon(Icons.search),
                            onPressed: () => _searchProducts(_searchController.text),
                          ),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(12.0),
                          ),
                        ),
                        onSubmitted: _searchProducts,
                      ),
                    ),
                    SizedBox(width: 8.0),
                    Container(
                      width: 130.0,
                      child: DropdownButtonFormField<String>(
                        value: _selectedCategory,
                        items: categories.map((String category) {
                          return DropdownMenuItem<String>(
                            value: category,
                            child: Text(category, overflow: TextOverflow.ellipsis),
                          );
                        }).toList(),
                        onChanged: _onCategoryChanged,
                        decoration: InputDecoration(
                          labelText: 'Categoria',
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(12.0),
                          ),
                        ),
                      ),
                    ),
                    SizedBox(width: 8.0),
                    Container(
                      width: 120.0,
                      child: DropdownButtonFormField<String>(
                        value: _selectedSortCriteria,
                        items: sortCriteria.map((String criteria) {
                          return DropdownMenuItem<String>(
                            value: criteria,
                            child: Text(criteria, overflow: TextOverflow.ellipsis),
                          );
                        }).toList(),
                        onChanged: _onSortCriteriaChanged,
                        decoration: InputDecoration(
                          labelText: 'Ordina per',
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(12.0),
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
                SizedBox(height: 8.0),
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _minPriceController,
                        decoration: InputDecoration(
                          hintText: 'Prezzo minimo',
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(12.0),
                          ),
                        ),
                        keyboardType: TextInputType.number,
                      ),
                    ),
                    SizedBox(width: 8.0),
                    Expanded(
                      child: TextField(
                        controller: _maxPriceController,
                        decoration: InputDecoration(
                          hintText: 'Prezzo massimo',
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(12.0),
                          ),
                        ),
                        keyboardType: TextInputType.number,
                      ),
                    ),
                    SizedBox(width: 8.0),
                    ElevatedButton(
                      onPressed: _searchProductsByPriceRange,
                      child: Text('Filtra'),
                      style: ElevatedButton.styleFrom(
                        padding: EdgeInsets.symmetric(vertical: 16.0, horizontal: 24.0),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12.0),
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : _products == null || _products!.isEmpty
          ? Center(child: Text('NESSUN PRODOTTO CON QUESTE CARATTERISTICHE'))
          : LayoutBuilder(
        builder: (context, constraints) {
          int crossAxisCount = 2;
          if (constraints.maxWidth > 750 && constraints.maxWidth <= 990) {
            crossAxisCount = 3;
          } else if (constraints.maxWidth > 990 && constraints.maxWidth < 1300) {
            crossAxisCount = 4;
          } else if (constraints.maxWidth >= 1300) {
            crossAxisCount = 5;
          }

          return Padding(
            padding: const EdgeInsets.all(8.0),
            child: GridView.builder(
              gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: crossAxisCount,
                childAspectRatio: 0.8,
                mainAxisSpacing: 16.0,
                crossAxisSpacing: 16.0,
              ),
              itemCount: _products?.length ?? 0,
              itemBuilder: (context, index) {
                final product = _products![index];
                final imagePath = categoryImageMap[product.categoria] ?? 'assets/images/default.jpg';
                int quantity = _quantities[product.id] ?? 1;

                return GestureDetector(
                  onTap: () => _showProductDetails(product),
                  child: Card(
                    margin: EdgeInsets.all(4.0),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: <Widget>[
                        AspectRatio(
                          aspectRatio: 1.0,
                          child: Image.asset(
                            imagePath,
                            fit: BoxFit.cover,
                          ),
                        ),
                        Padding(
                          padding: const EdgeInsets.all(8.0),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      product.nome,
                                      style: TextStyle(fontSize: 14),
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                    Text(
                                      '€${product.prezzo.toStringAsFixed(2)}',
                                      style: TextStyle(fontSize: 12),
                                    ),
                                  ],
                                ),
                              ),
                              Row(
                                children: [
                                  IconButton(
                                    icon: Icon(Icons.remove, color: Colors.red),
                                    onPressed: () {
                                      setState(() {
                                        if (quantity > 1) {
                                          quantity--;
                                          _quantities[product.id] = quantity;
                                        }
                                      });
                                    },
                                  ),
                                  Text(quantity.toString(), style: TextStyle(fontSize: 16.0)),
                                  IconButton(
                                    icon: Icon(Icons.add, color: Colors.green),
                                    onPressed: () {
                                      setState(() {
                                        quantity++;
                                        _quantities[product.id] = quantity;
                                      });
                                    },
                                  ),
                                ],
                              ),
                              IconButton(
                                icon: Icon(Icons.add_shopping_cart, color: Colors.blue),
                                onPressed: () => _addToCart(product.id),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          );
        },
      ),
    );
  }

}

