package systems.concurrent.crediversemobile.services

import android.content.Context
import systems.concurrent.crediversemobile.repositories.BundleRepository
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.view_models.*

// Finally a singleton which doesn't need anything passed to the constructor
object InjectorUtils {

    /**
     * For each of these factories
     *
     * The ModelFactory needs a repository, which in turn needs a Data Access Object (DAO)
     * The whole dependency tree is constructed right here, in one place
     */

    fun provideLoginFactory(context: Context): LoginViewModelFactory {
        return LoginViewModelFactory(MasRepository(context))
    }

    fun provideTransactionHistoryFactory(context: Context): TransactionHistoryViewModelFactory {
        return TransactionHistoryViewModelFactory(MasRepository(context))
    }

    fun provideAccountBalancesFactory(context: Context): AccountBalancesViewModelFactory {
        return AccountBalancesViewModelFactory(MasRepository(context))
    }

    fun provideAccountInfoFactory(context: Context): AccountInfoViewModelFactory {
        return AccountInfoViewModelFactory(MasRepository(context))
    }

    fun provideStatisticsFactory(context: Context): StatisticsViewModelFactory {
        return StatisticsViewModelFactory(MasRepository(context))
    }

    fun provideBundleFactory(): BundleViewModelFactory {
        return BundleViewModelFactory(BundleRepository())
    }

    fun provideMasTransactFactory(context: Context): TransactViewModelFactoryMasRepo {
        return TransactViewModelFactoryMasRepo(MasRepository(context))
    }

    fun provideMobileMoneyFactory(context: Context): MobileMoneyViewModelFactory {
        return MobileMoneyViewModelFactory(MasRepository(context))
    }
}
