// These sesstings are enabled now - as per SmartApp
const currencyFormatSettings = {
    locale: "fr-CI",
    currencyPattern: "#,##0 ¤",
    groupingSeparator: " ",
    decimalSeparator: ",",
    currencySymbol: "Fcfa",
    currencyCode: "XOF",
    negativeSymbol: "-",
    zeroSymbol: "0"
};

// Disabled settings
// const currencyFormatSettings = {
//     locale: "en-CI",
//     currencyPattern: "¤#,##0",
//     groupingSeparator: ",",
//     decimalSeparator: ".",
//     currencySymbol: "Fcfa",
//     negativeSymbol: "-",
//     zeroSymbol: "0"
// };

const formattedCurrency = (amount) => {
    const formatter = new Intl.NumberFormat(currencyFormatSettings.locale, {
        style: "currency",
        currency: currencyFormatSettings.currencyCode,
        currencyDisplay: "code",
        notation: "standard",
        pattern: currencyFormatSettings.currencyPattern.replace("¤", currencyFormatSettings.currencySymbol),
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
        minimumIntegerDigits: 1,
        groupingSeparator: currencyFormatSettings.groupingSeparator,
        decimalSeparator: currencyFormatSettings.decimalSeparator,
        negativePrefix: currencyFormatSettings.negativeSymbol,
        negativeSuffix: "",
        positivePrefix: "",
        positiveSuffix: "",
        zeroSuffix: currencyFormatSettings.zeroSymbol
    });
    return formatter.format(amount).replace(currencyFormatSettings.currencyCode, currencyFormatSettings.currencySymbol);
};

export { formattedCurrency }
