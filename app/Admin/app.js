function getFirebase() {
    if (!firebase.apps.length) {
        const firebaseConfig = {
            apiKey: "AIzaSyBWsMXm0_Z5XnGRg67iWmBmQXI9v6jsQ5Q",
            authDomain: "agro-chain-bc156.firebaseapp.com",
            projectId: "agro-chain-bc156",
            storageBucket: "agro-chain-bc156.appspot.com",
            messagingSenderId: "545018771587",
            appId: "1:545018771587:android:a94c2e53fd1873db6383b6"
        };
        firebase.initializeApp(firebaseConfig);
    }
    return firebase.firestore();
}

// Import Firebase modules
import { initializeApp } from 'https://www.gstatic.com/firebasejs/9.6.10/firebase-app.js';
import { getFirestore, collection, addDoc } from 'https://www.gstatic.com/firebasejs/9.6.10/firebase-firestore.js';
import { getStorage, ref, uploadBytes, getDownloadURL } from 'https://www.gstatic.com/firebasejs/9.6.10/firebase-storage.js';

const firebaseConfig = {
    apiKey: "AIzaSyBWsMXm0_Z5XnGRg67iWmBmQXI9v6jsQ5Q",
    authDomain: "agro-chain-bc156.firebaseapp.com",
    projectId: "agro-chain-bc156",
    storageBucket: "agro-chain-bc156.appspot.com",
    messagingSenderId: "545018771587",
    appId: "1:545018771587:android:a94c2e53fd1873db6383b6"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const db = getFirestore(app);
const storage = getStorage(app);

document.getElementById('productForm').addEventListener('submit', async (event) => {
    event.preventDefault(); // Prevent the form from submitting normally

    const name = document.getElementById('productName').value;
    const brand = document.getElementById('productBrand').value;
    const price = document.getElementById('productPrice').value;
    const size = document.getElementById('productSize').value;
    const OldPrice = document.getElementById('OldPrice').value;
    const description = document.getElementById('description').value;
    const imageFile = document.getElementById('imageFile').files[0];

    if (!imageFile) {
        alert("Please select an image file.");
        return;
    }

    const adminUserId = "GOVA-CyUsLz";
    const storageRef = ref(storage, `productImages/${imageFile.name}`);

    try {
        const snapshot = await uploadBytes(storageRef, imageFile);
        const imageUrl = await getDownloadURL(snapshot.ref);
        await addDoc(collection(db, "GOV", adminUserId, "PRODUCT_MARKET"), {
            name,
            brand,
            price,
            size,
            OldPrice,  
            description,
            imageUrl
        });
        alert("Product added successfully!");
        document.getElementById('productForm').reset();
    } catch (error) {
        console.error("Error adding product: ", error);
        alert("Error adding product: " + error.message);
    }
});
