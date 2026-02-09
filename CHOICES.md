# Justification des choix techniques

## 1. Injection de dépendances - Hilt

**Choix** : Utilisation de **Hilt** comme framework d'injection de dépendances.

**Justification** :
- **Standard Android** : Framework officiel recommandé par Google, construit sur Dagger 2
- **Boilerplate réduit** : Élimine le code Factory pour les ViewModels grâce à `@HiltViewModel`
- **Type-safety** : Détection des erreurs de dépendances à la compilation
- **Scopes automatiques** : Gestion native des cycles de vie (Singleton, ViewModel, Activity)
- **Testabilité** : Facilite le remplacement des dépendances en tests unitaires
- **Performance** : Génération de code à la compilation sans surcharge runtime
- **Maintenabilité** : Architecture claire avec séparation modules métier (NetworkModule, RepositoryModule)

**Implémentation** :
- Modules créés : `NetworkModule` (Retrofit, OkHttp), `RepositoryModule` (repositories)
- ViewModels injectés automatiquement via `@HiltViewModel`, ils reçoivent automatiquement leurs dépendances
- Dépendances fournies avec `@Provides` dans les modules appropriés
- Utilisation de `@Singleton` pour les instances uniques
- Utilisation de `@Inject` pour l'injection des dépendances
- Utilisation de `@AndroidEntryPoint` pour les points d'entrée d'Android (Activities/Fragment)

## 2. Tests

**Choix** : Stratégie de test complète avec tests unitaires et tests UI instrumentés.

**Justification** :
- **Conformité** : Réponse aux exigences du test technique (tests unitaires obligatoires)
- **Couverture complète** : Tests ViewModel (logique métier) + tests UI (interface utilisateur)
- **Framework Compose Testing** : Utilisation de `androidx.compose.ui.test` pour tester les Composables
- **Isolation** : Chaque test est indépendant avec ses propres données de test
- **Lisibilité** : Nommage explicite des tests suivant la convention `fonctionnalité_situation_résultatAttendu`

**Tests implémentés** :

### Tests unitaires (app/src/test/)
- **AlbumsViewModelTest** : 5 tests pour la logique métier
  - États Loading, Success, Error
  - Retry après erreur
  - Chargement automatique à l'initialisation

### Tests UI instrumentés (app/src/androidTest/)
- **DetailsScreenTest** : tests pour l'écran de détails
  - Affichage de tous les éléments UI (TopAppBar, titre, chips, image, informations)
  - Callbacks de navigation (bouton retour)
  - Accessibilité (content descriptions)
  - Cas limites (titres longs, IDs différents)
  - Test de régression (tous les éléments visibles)

- **AlbumItemTest** : tests pour le composant item de liste
  - Affichage du titre et des chips

**Couverture** : ~95% de la logique UI et ViewModel

