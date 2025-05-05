const Register = ({ onNavigate }) => {
    const [formData, setFormData] = React.useState({
        firstName: '',
        lastName: '',
        dateOfBirth: '',
        email: '',
        password: '',
        confirmPassword: ''
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
    
    // Check if user is at least 15 years old
    const isAtLeast15YearsOld = (dateOfBirth) => {
        if (!dateOfBirth) return false;
        
        const today = new Date();
        const birthDate = new Date(dateOfBirth);
        let age = today.getFullYear() - birthDate.getFullYear();
        const monthDiff = today.getMonth() - birthDate.getMonth();
        
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
        
        return age >= 15;
    };
    
    // Password strength validation
    const isPasswordStrong = (password) => {
        const regex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\S+$).{8,}$/;
        return regex.test(password);
    };
    
    const validateForm = () => {
        const newErrors = {};
        
        // First Name validation
        if (!formData.firstName.trim()) {
            newErrors.firstName = 'First name is required';
        }
        
        // Last Name validation
        if (!formData.lastName.trim()) {
            newErrors.lastName = 'Last name is required';
        }
        
        // Date of Birth validation
        if (!formData.dateOfBirth) {
            newErrors.dateOfBirth = 'Date of birth is required';
        } else if (!isAtLeast15YearsOld(formData.dateOfBirth)) {
            newErrors.dateOfBirth = 'You must be at least 15 years old';
        }
        
        // Email validation
        if (!formData.email) {
            newErrors.email = 'Email is required';
        } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
            newErrors.email = 'Email is invalid';
        }
        
        // Password validation
        if (!formData.password) {
            newErrors.password = 'Password is required';
        } else if (!isPasswordStrong(formData.password)) {
            newErrors.password = 'Password must be at least 8 characters and contain uppercase, lowercase, digit, and special character';
        }
        
        // Confirm Password validation
        if (!formData.confirmPassword) {
            newErrors.confirmPassword = 'Please confirm your password';
        } else if (formData.password !== formData.confirmPassword) {
            newErrors.confirmPassword = 'Passwords do not match';
        }
        
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };
    
    const checkEmailExists = async (email) => {
        try {
            const response = await fetch(`/api/check-email?email=${email}`);
            const data = await response.json();
            return data.exists;
        } catch (error) {
            console.error('Error checking email:', error);
            return false;
        }
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
            // Check if email exists
            const emailExists = await checkEmailExists(formData.email);
            if (emailExists) {
                setErrors({
                    ...errors,
                    email: 'Email already exists'
                });
                setIsSubmitting(false);
                return;
            }
            
            // Submit registration
            const response = await fetch('/api/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
            
            const data = await response.json();
            
            if (response.ok) {
                setAlertType('success');
                setAlertMessage('Registration successful! Redirecting to login...');
                
                // Redirect to login page after 2 seconds
                setTimeout(() => {
                    onNavigate('login');
                }, 2000);
            } else {
                setAlertType('danger');
                setAlertMessage(data.message || 'Registration failed');
            }
        } catch (error) {
            console.error('Registration error:', error);
            setAlertType('danger');
            setAlertMessage('An error occurred. Please try again later.');
        } finally {
            setIsSubmitting(false);
        }
    };
    
    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <h3>Create Account</h3>
                </div>
                <div className="auth-body">
                    {alertMessage && (
                        <div className={`alert alert-${alertType}`} role="alert">
                            {alertMessage}
                        </div>
                    )}
                    
                    <form onSubmit={handleSubmit}>
                        <div className="row mb-3">
                            <div className="col-md-6">
                                <label htmlFor="firstName" className="form-label">First Name</label>
                                <input
                                    type="text"
                                    className={`form-control ${errors.firstName ? 'is-invalid' : ''}`}
                                    id="firstName"
                                    name="firstName"
                                    value={formData.firstName}
                                    onChange={handleChange}
                                    required
                                />
                                {errors.firstName && <div className="invalid-feedback">{errors.firstName}</div>}
                            </div>
                            <div className="col-md-6">
                                <label htmlFor="lastName" className="form-label">Last Name</label>
                                <input
                                    type="text"
                                    className={`form-control ${errors.lastName ? 'is-invalid' : ''}`}
                                    id="lastName"
                                    name="lastName"
                                    value={formData.lastName}
                                    onChange={handleChange}
                                    required
                                />
                                {errors.lastName && <div className="invalid-feedback">{errors.lastName}</div>}
                            </div>
                        </div>
                        
                        <div className="mb-3">
                            <label htmlFor="dateOfBirth" className="form-label">Date of Birth</label>
                            <input
                                type="date"
                                className={`form-control ${errors.dateOfBirth ? 'is-invalid' : ''}`}
                                id="dateOfBirth"
                                name="dateOfBirth"
                                value={formData.dateOfBirth}
                                onChange={handleChange}
                                required
                            />
                            {errors.dateOfBirth && <div className="invalid-feedback">{errors.dateOfBirth}</div>}
                        </div>
                        
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
                            <div className="form-text password-criteria">
                                Password must be at least 8 characters and contain uppercase, lowercase, digit, and special character.
                            </div>
                            {errors.password && <div className="invalid-feedback">{errors.password}</div>}
                        </div>
                        
                        <div className="mb-3">
                            <label htmlFor="confirmPassword" className="form-label">Confirm Password</label>
                            <input
                                type="password"
                                className={`form-control ${errors.confirmPassword ? 'is-invalid' : ''}`}
                                id="confirmPassword"
                                name="confirmPassword"
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                required
                            />
                            {errors.confirmPassword && <div className="invalid-feedback">{errors.confirmPassword}</div>}
                        </div>
                        
                        <button
                            type="submit"
                            className="btn btn-primary w-100"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? (
                                <>
                                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                    Registering...
                                </>
                            ) : 'Register'}
                        </button>
                    </form>
                </div>
                <div className="auth-footer">
                    <p className="mb-0">
                        Already have an account?{' '}
                        <a href="#" onClick={() => onNavigate('login')}>
                            Login
                        </a>
                    </p>
                </div>
            </div>
        </div>
    );
}; 