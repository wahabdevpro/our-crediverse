use std::{
    fs::{self, File},
    io::Read,
};

use base64::{engine::general_purpose, Engine as _};

use openssl::{
    hash::MessageDigest,
    pkey::{PKey, Private},
    rsa::Rsa,
    sign::Signer,
};

use crate::{local_utils::get_env_config, mas::credivault_api};

impl std::fmt::Display for credivault_api::TransferRequest {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "({}, {}, {}, {}, {})",
            self.from_wallet, self.to_wallet, self.amount, self.ownership_key, self.at_time,
        )
    }
}

pub struct WalletOwner {
    pub private_key: PKey<Private>,
}

impl WalletOwner {
    pub fn pubilc_key_base64(&self) -> String {
        general_purpose::STANDARD_NO_PAD.encode(
            self.private_key
                .public_key_to_pem()
                .expect("could not get the public pem from the key"),
        )
    }

    pub fn new() -> WalletOwner {
        let private_key_filename = get_env_config(
            "MOBILE_MONEY_OWNER_PRIVATE_KEY_FILENAME".to_string(),
            "private_key.pem".to_string(),
        );
        let mut file = match File::open(private_key_filename) {
            Ok(file) => file,
            Err(_) => {
                let keypair = Rsa::generate(2048).unwrap();
                println!("Creating a new owner");
                let key = PKey::from_rsa(keypair).unwrap();

                let private_key_pem = key
                    .private_key_to_pem_pkcs8()
                    .expect("could not create pem from pirivate key");

                fs::write("private_key.pem", private_key_pem)
                    .expect("could not write private_key.pem");

                File::open("private_key.pem")
                    .expect("could not open private_key file, even after trying to create it! ")
            }
        };

        let mut private_key_data = vec![];
        match file.read_to_end(&mut private_key_data) {
            Ok(_) => {}
            Err(err) => {
                panic!("could not read the private_key.pem: {}", err)
            }
        }

        // Construct the private key from the data
        let private_key = PKey::private_key_from_pem(&private_key_data)
            .expect("could note create private key from file data");

        // Output the public key in PEM format
        let public_key_pem = private_key
            .public_key_to_pem()
            .expect("could not create public key");

        println!(
            "Public Key (PEM): {}",
            String::from_utf8(public_key_pem).unwrap()
        );

        // load the key pair from the files

        WalletOwner { private_key }
    }

    pub fn sign_request(&self, representation: String) -> String {
        println!("String to sign: '{}'", representation);
        println!("public key of signer: '{}'", self.pubilc_key_base64());

        let mut signer = Signer::new(MessageDigest::sha256(), &self.private_key)
            .expect("Could not create the signer");

        signer
            .update(representation.as_bytes())
            .expect("could not update the signer with the message");

        // Sign the data
        let signature = signer.sign_to_vec().expect("could not sign the request");

        // Base64 encode the signature
        general_purpose::STANDARD_NO_PAD.encode(signature)
    }

    pub fn sign_transfer(&self, request: credivault_api::TransferRequest) -> String {
        let representation = request.to_string();

        println!("String to sign: '{}'", representation);

        self.sign_request(representation)
    }
}
