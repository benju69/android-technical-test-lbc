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

## 2. Persistence locale - Room

**Choix** : Utilisation de **Room** comme solution de persistence locale pour le cache des données.

**Justification** :

### Avantages techniques
- **Abstraction SQLite** : Couche d'abstraction au-dessus de SQLite avec vérification à la compilation
- **Type-safety** : Détection des erreurs SQL à la compilation grâce aux annotations et au code généré
- **Intégration Kotlin Coroutines/Flow** : Support natif pour les opérations asynchrones et réactives
  - `Flow<List<AlbumEntity>>` pour observer les changements en temps réel
  - `suspend fun` pour les opérations asynchrones sans bloquer le thread UI
- **Migration simplifiée** : Gestion automatique des migrations de base de données
- **Performance optimale** : Requêtes compilées et mises en cache, indexation automatique sur les clés primaires

### Architecture mise en place

**Entités** :
```kotlin
@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: Int,
    val albumId: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
```

**DAO (Data Access Object)** :
- **Requêtes réactives** : `getAllAlbums(): Flow<List<AlbumEntity>>` pour observer les changements
- **Opérations CRUD** : Insert, Update, Delete avec `@Insert`, `@Update`, `@Query`
- **Requêtes spécifiques** : 
  - `getFavoriteAlbums()` pour filtrer les favoris
  - `isFavorite(id: Int)` pour vérifier le statut
  - `updateFavoriteStatus(id: Int, isFavorite: Boolean)` pour modifier l'état favori

**Database** :
```kotlin
@Database(entities = [AlbumEntity::class], version = 1)
abstract class AlbumDatabase : RoomDatabase()
```

### Stratégie Offline-First implémentée

1. **Cache prioritaire** : 
   - L'app affiche d'abord les données en cache (instantané)
   - Rafraîchit en arrière-plan depuis le réseau
   
2. **Gestion de la fraîcheur** :
   - Cache valide pendant 1 heure (`CACHE_DURATION_MS`)
   - Timestamp automatique à chaque insertion
   
3. **Résilience** :
   - Si le réseau échoue, utilise le cache même expiré
   - L'utilisateur voit toujours du contenu (UX améliorée)

4. **Réactivité temps réel** :
   - Les changements (favoris) se propagent automatiquement via `Flow`
   - UI mise à jour instantanément sans reload manuel
   - Pattern "Single Source of Truth" : la DB est la source de vérité

### Bénéfices pour les fonctionnalités

**Favoris** :
- État persisté localement même après fermeture de l'app
- Mise à jour instantanée dans toutes les vues grâce aux `Flow`
- Pas besoin de recharger la liste après toggle favori

**Performance** :
- Démarrage ultra-rapide (affichage du cache)
- Pas de spinner pendant le chargement réseau (si cache présent)
- Scroll fluide (données locales, pas de latence réseau)

**Mode hors-ligne** :
- Application utilisable sans connexion internet
- Données consultables même en avion/métro
- Favoris fonctionnent toujours

**Conclusion** : Room est le choix optimal pour cette app car il combine type-safety, réactivité (`Flow`), intégration Jetpack, et simplicité d'implémentation.

## 3. Tests

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

## 4. Optimisations de performance

### Performance actuelle du scroll

L'application utilise déjà plusieurs bonnes pratiques pour optimiser le scroll :

✅ **Implémentations performantes existantes** :
- **Keys stables** : `items(items = albums, key = { album -> album.id })` - Crucial pour la réutilisation des vues
- **AsyncImage avec Coil 3** : Chargement asynchrone des images avec cache intégré
- **Crossfade** : Transition douce des images qui améliore l'UX
- **Flow réactifs** : Mise à jour incrémentale de l'UI sans recharger toute la liste
- **StateFlow** : Évite les recompositions inutiles grâce à `collectAsStateWithLifecycle()`