const Login = ({ onNavigate, onLogin }) => {
    const [formData, setFormData] = React.useState({
        email: '',
        password: ''
    });
    const [errors, setErrors] = React.useState({});
    const [isSubmitting, setIsSubmitting] = React.useState(false);
    const [alertMessage, setAlertMessage] = React.useState('');
    const [alertType, setAlertType] = React.useState('');
    
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });
        
        // Clear validation errors when user types
        if (errors[name]) {
            setErrors({
                ...errors,
                [name]: ''
            });
        }
    };
    
    const validateForm = () => {
        const newErrors = {};
        
        // Email validation
        if (!formData.email) {
            newErrors.email = 'Email is required';
        } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
            newErrors.email = 'Email is invalid';
        }
        
        // Password validation
        if (!formData.password) {
            newErrors.password = 'Password is required';
        }
        
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };
    
    const handleSubmit = async (e) => {
        e.preventDefault();
        
        // Validate form
        if (!validateForm()) {
            return;
        }
        
        setIsSubmitting(true);
        setAlertMessage('');
        
        try {
            const response = await fetch('/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
            
            const data = await response.json();
            
            if (response.ok) {
                setAlertType('success');
                setAlertMessage('Login successful! Redirecting...');
                
                // Get user details
                const userResponse = await fetch('/api/user/me');
                if (userResponse.ok) {
                    const userData = await userResponse.json();
                    onLogin(userData);
                    onNavigate('dashboard');
                }
            } else {
                setAlertType('danger');
                setAlertMessage(data.message || 'Invalid email or password');
            }
        } catch (error) {
            console.error('Login error:', error);
            setAlertType('danger');
            setAlertMessage('An error occurred. Please try again later.');
        } finally {
            setIsSubmitting(false);
        }
    };
    
    const handleOAuthLogin = (provider) => {
        window.location.href = `/oauth2/authorization/${provider}`;
    };
    
    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <h3>Login</h3>
                </div>
                <div className="auth-body">
                    {alertMessage && (
                        <div className={`alert alert-${alertType}`} role="alert">
                            {alertMessage}
                        </div>
                    )}
                    
                    <form onSubmit={handleSubmit}>
                        <div className="mb-3">
                            <label htmlFor="email" className="form-label">Email</label>
                            <input
                                type="email"
                                className={`form-control ${errors.email ? 'is-invalid' : ''}`}
                                id="email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                required
                            />
                            {errors.email && <div className="invalid-feedback">{errors.email}</div>}
                        </div>
                        
                        <div className="mb-3">
                            <label htmlFor="password" className="form-label">Password</label>
                            <input
                                type="password"
                                className={`form-control ${errors.password ? 'is-invalid' : ''}`}
                                id="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                required
                            />
                            {errors.password && <div className="invalid-feedback">{errors.password}</div>}
                        </div>
                        
                        <div className="mb-3 form-check">
                            <input
                                type="checkbox"
                                className="form-check-input"
                                id="remember-me"
                                name="remember-me"
                            />
                            <label className="form-check-label" htmlFor="remember-me">Remember me</label>
                        </div>
                        
                        <button
                            type="submit"
                            className="btn btn-primary w-100"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? (
                                <>
                                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                    Logging in...
                                </>
                            ) : 'Login'}
                        </button>
                    </form>
                    
                    <div className="mt-4">
                        <p className="text-center mb-3">Or login with</p>
                        <button
                            onClick={() => handleOAuthLogin('google')}
                            className="btn btn-light oauth-btn border mb-2"
                            type="button"
                        >
                            <img src="https://upload.wikimedia.org/wikipedia/commons/5/53/Google_%22G%22_Logo.svg" alt="Google" />
                            Google
                        </button>
                        <button
                            onClick={() => handleOAuthLogin('facebook')}
                            className="btn btn-light oauth-btn border"
                            type="button"
                        >
                            <img src="https://upload.wikimedia.org/wikipedia/commons/5/51/Facebook_f_logo_%282019%29.svg" alt="Facebook" />
                            Facebook
                        </button>
                    </div>
                </div>
                <div className="auth-footer">
                    <p className="mb-0">
                        Don't have an account?{' '}
                        <a href="#" onClick={() => onNavigate('register')}>
                            Register
                        </a>
                    </p>
                </div>
            </div>
        </div>
    );
}; 