import jwt from 'jsonwebtoken';
// const SECRET_KEY = process.env.CREDISTATS_JWT_SECRET;
const SECRET_KEY ='ThisIsTotallyASecret'; 


// Middleware to verify a token
function verifyToken(req, res, next) {
    // Get the token from header, URL parameters, or post parameters
    let token = req.headers['authorization'];

    // Check if token exists
    if (!token) {
        return res.status(403).send({ message: 'No token provided.' });
    }

    // If token has the "Bearer" prefix, extract the actual token
    if (token.startsWith('Bearer ')) {
        token = token.slice(7, token.length);
    }

    // Verify the token
    jwt.verify(token, SECRET_KEY, (err, decoded) => {
        if (err) {
            return res.status(500).send({ message: 'Failed to authenticate token.' });
        }

        // If token is verified, set the decoded object to the request for use in other routes
        req.user = decoded;
        next();
    });
}

export { verifyToken, SECRET_KEY }


